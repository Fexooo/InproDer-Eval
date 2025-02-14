package de.felixkat.inprodereval

import de.felixkat.InproDer.derivationtrees.exporter.exportAsDotGraph
import de.felixkat.InproDer.derivationtrees.generateDerivationTree
import de.felixkat.InproDer.helper.findLValueFromParameter
import de.felixkat.InproDer.privacyflowgraphs.exporter.exportAsDotGraph
import de.felixkat.InproDer.privacyflowgraphs.generatePrivacyFlowGraph
import de.felixkat.InproDer.privacyflowgraphs.model.GlobalDataFlow
import org.tudo.sse.MavenCentralAnalysis
import org.tudo.sse.model.Artifact
import org.tudo.sse.model.index.Package
import sootup.core.inputlocation.AnalysisInputLocation
import sootup.core.model.SootClass
import sootup.core.model.SootMethod
import sootup.core.signatures.MethodSignature
import sootup.core.types.ClassType
import sootup.core.views.View
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation
import sootup.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation
import sootup.java.core.views.JavaView
import java.io.File
import java.util.*


class MavenCentralAnalyzer : MavenCentralAnalysis() {
    var analyzedArtifacts = 0

    init {
        resolveJar = true

        // Create Analysis file
        File("results/analysis.csv").writeText("Artifact;Choosen Method;Choosen Variable;Classes;Methods;Fields;Runtime DT;Runtime PFG;Runtime PFGDT\n")
    }

    override fun analyzeArtifact(toAnalyze: Artifact) {
        var jarFilePath = toAnalyze.ident.groupID + "-" + toAnalyze.ident.artifactID + "-" + toAnalyze.ident.version + ".jar"
        println("Analyzing Artifact: $jarFilePath")
        try {
            analyze(jarFilePath)
        } catch (e: Exception) {
            println("Error analyzing $jarFilePath")
            e.printStackTrace()
            File("errors/$jarFilePath.err").writeText(e.stackTraceToString())
        }
        analyzedArtifacts++;
    }

    fun analyze(fileName: String) {
        val locations = listOf(
            JrtFileSystemAnalysisInputLocation(),
            JavaClassPathAnalysisInputLocation("temp/$fileName")
        )

        val view: View = JavaView(locations)
        val artifactClasses = view.classes.size
        val artifactMethods = view.classes.sumOf { it.methods.size }
        val artifactFields = view.classes.sumOf { it.fields.size }

        var sootClass: SootClass
        var sootMethod: SootMethod
        do {
            do {
                do {
                    // Get random class from view.classes
                    var randomClassIndex = (0 until view.classes.size).random()
                    sootClass = view.classes.toList()[randomClassIndex]
                } while (sootClass.methods.isEmpty())
                var randomMethodIndex = (0 until sootClass.methods.size).random()
                sootMethod = sootClass.methods.toList()[randomMethodIndex]
            } while (!sootMethod.isConcrete)
        } while(sootMethod.body.defs.isEmpty())
        // Get random variable from method

        var randomDefIndex = (0 until sootMethod.body.defs.size).random()
        var variable = sootMethod.body.defs.toList()[randomDefIndex]
        println("Running eval on $fileName with class ${sootClass.type.className} and method ${sootMethod.signature} and variable $variable")

        if(variable != null) {
            println("Warming up DT JVM")
            for (i in 0..100) {
                generateDerivationTree(variable, sootMethod, sootClass, view)
            }

            // Derivation Tree
            val startTimeDT = System.nanoTime()
            val tree = generateDerivationTree(variable, sootMethod, sootClass, view)
            val endTimeDT = System.nanoTime()
            var derivationTreeTime = endTimeDT - startTimeDT
            val folderName = fileName.replace(".jar", "")
            // Make directory for file if it does not exist
            File("results/$folderName/").mkdirs()
            // Save file to disk
            File("results/$folderName/derivation-tree.dot").writeText(tree.exportAsDotGraph())

            println("Warming up PFG JVM")
            for (i in 0..100) {
                generatePrivacyFlowGraph(view, listOf(sootMethod.signature), false)
            }

            // Privacy Flow Graph
            val startTimePFG = System.nanoTime()
            val graphs = generatePrivacyFlowGraph(view, listOf(sootMethod.signature), false)
            val endTimePFG = System.nanoTime()
            var privacyFlowGraphTime = endTimePFG - startTimePFG
            var graphID = 0
            graphs.forEach { graph ->
                File("results/$folderName/pfg").mkdirs()
                File("results/$folderName/pfg/privacy-flow-$graphID.dot").writeText(graph.exportAsDotGraph())
                graphID++;
            }

            // Privacy Flow Graph + Derivation Trees
            val startTimePFGDT = System.nanoTime()
            val dtgraphs = generatePrivacyFlowGraph(view, listOf(sootMethod.signature), true)
            val endTimePFGDT = System.nanoTime()
            var privacyFlowGraphDerivationTreeTime = endTimePFGDT - startTimePFGDT
            graphID = 0
            dtgraphs.forEach { graph ->
                File("results/$folderName/pfgdt").mkdirs()
                File("results/$folderName/pfgdt/privacy-flow-$graphID.dot").writeText(graph.exportAsDotGraph())
                exportDerivationTree(folderName, graph, graphID)
                graphID++;
            }

            // Append to analysis file
            File("results/analysis.csv").appendText("$fileName;${sootMethod.signature};$variable;$artifactClasses;$artifactMethods;$artifactFields;$derivationTreeTime;$privacyFlowGraphTime;$privacyFlowGraphDerivationTreeTime\n")
            println("Finished analyzation of $fileName. Choosen Method: ${sootMethod.signature}, Choosen Variable: ${variable} Classes: $artifactClasses, Methods: $artifactMethods, Fields: $artifactFields, Derivation Tree Time: $derivationTreeTime, Privacy Flow Graph Time: $privacyFlowGraphTime, Privacy Flow Graph + Derivation Trees Time: $privacyFlowGraphDerivationTreeTime")
        }
    }

    fun exportDerivationTree(folderName: String, flow: GlobalDataFlow, graphID: Int) {
        flow.node.derivationNode.forEach { node ->
            File("results/$folderName/pfgdt/privacy-flow-$graphID/derivation-trees-${flow.node.method.declClassType.className}-${flow.node.method.name.hashCode()}/").mkdirs()
            File("results/$folderName/pfgdt/privacy-flow-$graphID/derivation-trees-${flow.node.method.declClassType.className}-${flow.node.method.name.hashCode()}/${node.variableName}.dot").writeText(node.exportAsDotGraph())
        }
        flow.call.forEach { exportDerivationTree(folderName, it, graphID) }
    }

}

fun main(args: Array<String>) {
    println("Evaluation started")
    //manualAnalyzation()
    var analyzer = MavenCentralAnalyzer()
    analyzer.runAnalysis(args)
    println("Evaluation finished")
}

fun manualAnalyzation() {
    val locations = listOf<AnalysisInputLocation>(
        JrtFileSystemAnalysisInputLocation(),
        //JavaClassPathAnalysisInputLocation("/Users/felixkatzenberg/Developer/Studium/Bachelor Arbeit/jabref/build/distributions/JabRef-100.0.0/lib/commons-cli-1.9.0.jar"),
        //JavaClassPathAnalysisInputLocation("/Users/felixkatzenberg/Developer/Studium/Bachelor Arbeit/jabref/build/libs/JabRef-100.0.0.jar"),
        JavaClassPathAnalysisInputLocation("src/test/java/example/")
    )

    val view: View = JavaView(locations);
    val classType: ClassType = view.identifierFactory.getClassType("Status");
    //val classType: ClassType = view.identifierFactory.getClassType("org.jabref.logic.importer.fetcher.DoiFetcher");
    //val classType: ClassType = view.identifierFactory.getClassType("org.jabref.Launcher")

    val methodSignature: MethodSignature =
        view.identifierFactory
            .getMethodSignature(
                classType, "main", "void", Collections.singletonList("java.lang.String[]"));
                //classType, "performSearchById", "java.util.Optional", Collections.singletonList("java.lang.String"));

    if (!view.getClass(classType).isPresent) {
        System.out.println("Class not found!");
        return;
    }

    val sootClass: SootClass = view.getClass(classType).get();

    println(sootClass.methods)
    view.getMethod(methodSignature);

    if (!sootClass.getMethod(methodSignature.subSignature).isPresent) {
        System.out.println("Method not found!");
        return;  // Exit if the method is not found
    }

    val sootMethod: SootMethod = sootClass.getMethod(methodSignature.subSignature).get();
    println("defs: " + sootMethod.body.defs)

    //val tree = generateDerivationTree(findLValueFromParameter(0, sootMethod.body.stmtGraph.stmts).get(), sootMethod, sootClass, view)
    val tree = generateDerivationTree("args", sootMethod, sootClass, view)
    tree.printTree()
    // Make directory for file if it does not exist
    File("results/status2/").mkdirs()
    // Save file to disk
    File("results/status2/derivation-tree.dot").writeText(tree.exportAsDotGraph())

    var graphs = generatePrivacyFlowGraph(view, ::sourceMethodDefinitionStudent, true)
    var graphID = 0
    graphs.forEach { graph ->
        File("results/status2/privacy-flow-$graphID.dot").writeText(graph.exportAsDotGraph())
        File("results/status2/privacy-flow-$graphID/").mkdirs()
        exportDerivationTree(graph, graphID)
        graphID++;
    }

}

fun exportDerivationTree(flow: GlobalDataFlow, graphID: Int) {
    flow.node.derivationNode.forEach { node ->
        File("results/status2/privacy-flow-$graphID/derivation-trees-${flow.node.method.declClassType.className}-${flow.node.method.name}/").mkdirs()
        File("results/status2/privacy-flow-$graphID/derivation-trees-${flow.node.method.declClassType.className}-${flow.node.method.name}/${node.variableName}.dot").writeText(node.exportAsDotGraph())
    }
    flow.call.forEach { exportDerivationTree(it, graphID) }
}

fun sourceMethodDefinition(method: SootMethod): Boolean {
    return method.signature.toString().contains("init") && method.signature.declClassType.packageName.name.contains("org.jabref.logic.net") && method.signature.parameterTypes.any { it.toString().contains("java.net.URL") }
}

fun sourceMethodDefinitionStudent(method: SootMethod): Boolean {
    return method.signature.toString().contains("studentread")
}