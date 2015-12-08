#include "JType.h"
#include "NativeScriptAssert.h"

namespace tns
{
	Type JType::getClassType(int retType) {
		Type classReturnType = static_cast<Type>(retType);
		return classReturnType;
	}

#define DEFINE_GETPRIMITIVETYPEINFO_METHOD(type, typeSig, ctorSig, fieldSig) \
template<> \
JType::PrimitiveTypeInfo& JType::PrimitiveTypeInfo::GetPrimitiveTypeInfo<static_cast<int>(type)>() \
{ \
	auto& pti = JType::PrimitiveTypeInfo::s_primitiveTypeCache[static_cast<int>(type)]; \
	if (pti.clazz == nullptr) \
	{ \
		JEnv env; \
		pti.clazz = env.FindClass(typeSig); \
		pti.ctorID = env.GetMethodID(pti.clazz, "<init>", ctorSig); \
		pti.fieldID = env.GetFieldID(pti.clazz, "value", fieldSig); \
	} \
	return pti; \
}

	DEFINE_GETPRIMITIVETYPEINFO_METHOD(Type::Boolean, "java/lang/Boolean", "(Z)V", "Z")
	DEFINE_GETPRIMITIVETYPEINFO_METHOD(Type::Char, "java/lang/Character", "(C)V", "C")
	DEFINE_GETPRIMITIVETYPEINFO_METHOD(Type::Byte, "java/lang/Byte", "(B)V", "B")
	DEFINE_GETPRIMITIVETYPEINFO_METHOD(Type::Short, "java/lang/Short", "(S)V", "S")
	DEFINE_GETPRIMITIVETYPEINFO_METHOD(Type::Int, "java/lang/Integer", "(I)V", "I")
	DEFINE_GETPRIMITIVETYPEINFO_METHOD(Type::Long, "java/lang/Long", "(J)V", "J")
	DEFINE_GETPRIMITIVETYPEINFO_METHOD(Type::Float, "java/lang/Float", "(F)V", "F")
	DEFINE_GETPRIMITIVETYPEINFO_METHOD(Type::Double, "java/lang/Double", "(D)V", "D")


#define DEFINE_PRIMITIVE_TYPE_HELPER_METHODS(type, jnitype) \
jobject JType::New##type(JEnv env, jnitype value) \
{ \
	auto& pti = JType::PrimitiveTypeInfo::GetPrimitiveTypeInfo<static_cast<int>(Type::type)>(); \
	return env.NewObject(pti.clazz, pti.ctorID, value); \
} \
jnitype JType::type##Value(JEnv env, jobject value) \
{ \
	auto& pti = JType::PrimitiveTypeInfo::GetPrimitiveTypeInfo<static_cast<int>(Type::type)>(); \
	jnitype ret = env.Get##type##Field(value, pti.fieldID); \
	return ret; \
}

	DEFINE_PRIMITIVE_TYPE_HELPER_METHODS(Boolean, jboolean)
	DEFINE_PRIMITIVE_TYPE_HELPER_METHODS(Char, jchar)
	DEFINE_PRIMITIVE_TYPE_HELPER_METHODS(Byte, jbyte)
	DEFINE_PRIMITIVE_TYPE_HELPER_METHODS(Short, jshort)
	DEFINE_PRIMITIVE_TYPE_HELPER_METHODS(Int, jint)
	DEFINE_PRIMITIVE_TYPE_HELPER_METHODS(Long, jlong)
	DEFINE_PRIMITIVE_TYPE_HELPER_METHODS(Float, jfloat)
	DEFINE_PRIMITIVE_TYPE_HELPER_METHODS(Double, jdouble)

	JType::PrimitiveTypeInfo JType::PrimitiveTypeInfo::s_primitiveTypeCache[static_cast<int>(Type::PrimitiveTypeCount)];
}
