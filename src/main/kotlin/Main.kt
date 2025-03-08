package de.felixkat.inprodereval

import de.felixkat.InproDer.derivationtrees.exporter.exportAsDotGraph
import de.felixkat.InproDer.derivationtrees.generateDerivationTree
import de.felixkat.InproDer.helper.findLValueFromParameter
import de.felixkat.InproDer.privacyflowgraphs.exporter.exportAsDotGraph
import de.felixkat.InproDer.privacyflowgraphs.generatePrivacyFlowGraph
import de.felixkat.InproDer.privacyflowgraphs.model.GlobalDataFlow
import org.tudo.sse.MavenCentralAnalysis
import org.tudo.sse.model.Artifact
import sootup.core.inputlocation.AnalysisInputLocation
import sootup.core.jimple.basic.LValue
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
        JavaClassPathAnalysisInputLocation("Path/to/your/jar"), // Change to jar file, which you want to analyze
    )

    val view: View = JavaView(locations);
    val artifactClasses = view.classes.size
    val artifactMethods = view.classes.sumOf { it.methods.size }
    val artifactFields = view.classes.sumOf { it.fields.size }
    println("View data:\n   Classes: $artifactClasses\n   Methods: $artifactMethods\n   Fields: $artifactFields")

    val classType: ClassType = view.identifierFactory.getClassType("your.class.identifier"); // Change to desired class identifier for derivation tree usage

    val methodSignature: MethodSignature =
        view.identifierFactory
            .getMethodSignature(
                classType, "main", "void", Collections.singletonList("java.lang.String[]")); // Your method signature for derivation tree usage

    if (!view.getClass(classType).isPresent) {
        System.out.println("Class not found!");
        return;
    }

    // Retrieving class and method from view
    val sootClass: SootClass = view.getClass(classType).get();
    view.getMethod(methodSignature);

    // Check if method is found
    if (!sootClass.getMethod(methodSignature.subSignature).isPresent) {
        System.out.println("Method not found!");
        return;  // Exit if the method is not found
    }

    // Get SootMethod from class
    val sootMethod: SootMethod = sootClass.getMethod(methodSignature.subSignature).get();

    // Generate derivation tree, change "args" to your desired variable or use callback as shown below
    val tree = generateDerivationTree("args", sootMethod, sootClass, view, false)
    //val tree = generateDerivationTree(::variableDefinition, sootMethod, sootClass, view, false)

    // Print tree to console
    tree.printTree()

    // Make directory for results as dot graphs if it does not exist
    File("results/name/").mkdirs()
    // Save derivation tree dot graph to disk
    File("results/name/derivation-tree.dot").writeText(tree.exportAsDotGraph())

    // Generate privacy flow graphs
    var graphs = generatePrivacyFlowGraph(view, ::sourceMethodDefinition, true)

    // Save privacy flow dot graphs to disk
    var graphID = 0
    graphs.forEach { graph ->
        File("results/name/privacy-flow-$graphID.dot").writeText(graph.exportAsDotGraph())
        File("results/name/privacy-flow-$graphID/").mkdirs()
        exportDerivationTree("name", graph, graphID)
        graphID++;
    }

}

/*
 * Function to export derivation trees from privacy flows
 */
fun exportDerivationTree(pkg: String, flow: GlobalDataFlow, graphID: Int) {
    flow.node.derivationNode.forEach { node ->
        File("results/$pkg/privacy-flow-$graphID/derivation-trees-${flow.node.method.declClassType.className}-${flow.node.method.name}/").mkdirs()
        File("results/$pkg/privacy-flow-$graphID/derivation-trees-${flow.node.method.declClassType.className}-${flow.node.method.name}/${node.variableName}.dot").writeText(node.exportAsDotGraph())
    }
    flow.call.forEach { exportDerivationTree(pkg, it, graphID) }
}

// Example variable callback definition
fun variableDefinition(lValue: LValue): Boolean {
    return lValue.toString() == "args" // Return true if variable is containing your desired variable characteristic
}

// Example source method callback definition
fun sourceMethodDefinition(method: SootMethod): Boolean {
    return method.signature.toString().contains("main") // Return true if method is containing your desired method characteristic
}