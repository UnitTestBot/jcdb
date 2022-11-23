package org.utbot.jcdb.api.ext

import kotlinx.collections.immutable.toImmutableList
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.utbot.jcdb.api.*

/**
 * find all methods used in bytecode of specified `method`
 * @param method method to analyze
 */
fun JcClasspath.findMethodsUsedIn(method: JcMethod): List<JcMethod> {
    val methodNode = method.body()
    val result = LinkedHashSet<JcMethod>()
    methodNode.instructions.forEach { instruction ->
        when (instruction) {
            is MethodInsnNode -> {
                val owner = Type.getObjectType(instruction.owner).className
                val clazz = findClassOrNull(owner)
                if (clazz != null) {
                    clazz.findMethodOrNull(instruction.name, instruction.desc)?.also {
                        result.add(it)
                    }
                }
            }
        }
    }
    return result.toImmutableList()
}

class FieldUsagesResult(
    val reads: List<JcField>,
    val writes: List<JcField>
) {
    companion object {
        val EMPTY = FieldUsagesResult(emptyList(), emptyList())
    }
}

/**
 * find all methods used in bytecode of specified `method`
 * @param method method to analyze
 */
fun JcClasspath.findFieldsUsedIn(method: JcMethod): FieldUsagesResult {
    val methodNode = method.body()
    val reads = LinkedHashSet<JcField>()
    val writes = LinkedHashSet<JcField>()
    methodNode.instructions.forEach { instruction ->
        when (instruction) {
            is FieldInsnNode -> {
                val owner = Type.getObjectType(instruction.owner).className
                val clazz = findClassOrNull(owner)
                if (clazz != null) {
                    clazz.findFieldOrNull(instruction.name)?.also {
                        when (instruction.opcode) {
                            Opcodes.GETFIELD -> reads.add(it)
                            Opcodes.GETSTATIC -> reads.add(it)
                            Opcodes.PUTFIELD -> writes.add(it)
                            Opcodes.PUTSTATIC -> writes.add(it)
                        }
                    }
                }
            }
        }
    }
    return FieldUsagesResult(
        reads = reads.toImmutableList(),
        writes = writes.toImmutableList()
    )

}


inline fun <reified T> JcClasspath.findClassOrNull(): JcClassOrInterface? {
    return findClassOrNull(T::class.java.name)
}

inline fun <reified T> JcClasspath.findTypeOrNull(): JcType? {
    return findClassOrNull(T::class.java.name)?.let {
        typeOf(it)
    }
}

fun JcClasspath.findTypeOrNull(typeName: TypeName): JcType? {
    return findTypeOrNull(typeName.typeName)
}


/**
 * find class. Tf there are none then throws `NoClassInClasspathException`
 * @throws NoClassInClasspathException
 */
fun JcClasspath.findClass(name: String): JcClassOrInterface {
    return findClassOrNull(name) ?: name.throwClassNotFound()
}

/**
 * find class. Tf there are none then throws `NoClassInClasspathException`
 * @throws NoClassInClasspathException
 */
inline fun <reified T> JcClasspath.findClass(): JcClassOrInterface {
    return findClassOrNull<T>() ?: throwClassNotFound<T>()
}

/**
 * find a common supertype for a set of classes
 */
fun JcClasspath.findCommonSupertype(types: Set<JcType>): JcType? = when {
    types.size == 1 -> types.first()
    types.all { it.typeName in integersMap } -> types.maxByOrNull { integersMap[it.typeName]!! }
    types.all { it is JcClassType } -> {
        val classes = types.map { it as JcClassType }
        var result = findTypeOrNull<Any>()!!
        for (i in 0..classes.lastIndex) {
            val isAncestor = classes.fold(true) { acc, klass ->
                acc && klass.jcClass isSubtypeOf classes[i].jcClass
            }

            if (isAncestor) {
                result = classes[i]
            }
        }
        result
    }
    types.all { it is JcRefType } -> when {
        types.any { it is JcClassType } -> findTypeOrNull<Any>()
        types.map { it as JcArrayType }.map { it.elementType }.toSet().size == 1 -> types.first()
        types.all { it is JcArrayType } -> {
            val components = types.map { (it as JcArrayType).elementType }.toSet()
            when (val merged = findCommonSupertype(components)) {
                null -> findTypeOrNull<Any>()
                else -> arrayTypeOf(merged)
            }
        }
        else -> findTypeOrNull<Any>()
    }
    else -> null
}

private val integersMap get() = mapOf(
    PredefinedPrimitives.boolean to 1,
    PredefinedPrimitives.byte to 8,
    PredefinedPrimitives.char to 8,
    PredefinedPrimitives.short to 16,
    PredefinedPrimitives.int to 32,
    PredefinedPrimitives.long to 64
)
