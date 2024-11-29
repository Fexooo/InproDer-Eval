package de.felixkat.inprodereval

import de.felixkat.InproDer.generateDerivationTree
import sootup.core.inputlocation.AnalysisInputLocation
import sootup.core.model.SootClass
import sootup.core.model.SootMethod
import sootup.core.signatures.MethodSignature
import sootup.core.types.ClassType
import sootup.core.util.DotExporter
import sootup.core.views.View
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation
import sootup.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation
import sootup.java.core.views.JavaView
import java.util.*

fun main() {
    val locations = listOf<AnalysisInputLocation>(
        JrtFileSystemAnalysisInputLocation(),
        JavaClassPathAnalysisInputLocation("src/test/java/example/")
    )

    val view: View = JavaView(locations);

    val classType: ClassType = view.identifierFactory.getClassType("HelloWorld");

    val methodSignature: MethodSignature =
        view.identifierFactory
            .getMethodSignature(
                classType, "main", "void", Collections.singletonList("java.lang.String[]"));

    if (!view.getClass(classType).isPresent()) {
        System.out.println("Class not found!");
        return;
    }

    val sootClass: SootClass = view.getClass(classType).get();

    view.getMethod(methodSignature);

    if (!sootClass.getMethod(methodSignature.getSubSignature()).isPresent()) {
        System.out.println("Method not found!");
        return;  // Exit if the method is not found
    }

    val sootMethod: SootMethod = sootClass.getMethod(methodSignature.getSubSignature()).get();

    val tree = generateDerivationTree("name", sootMethod, view)
    tree.printTree()
}