#ifndef JNIPRIMITIVETYPE_H_
#define JNIPRIMITIVETYPE_H_

#include "JEnv.h"

namespace tns
{
	enum class Type : int
	{
		Boolean				= 0,
		Char				= 1,
		Byte				= 2,
		Short				= 3,
		Int					= 4,
		Long				= 5,
		Float				= 6,
		Double				= 7,
		PrimitiveTypeCount	= 8,
		String				= 8,
		JsObject			= 9,
		Null				= 10
	};

	class JType
	{
	public:
		JType() = delete;

		static jobject NewByte(JEnv env, jbyte value);
		static jobject NewChar(JEnv env, jchar value);
		static jobject NewBoolean(JEnv env, jboolean value);
		static jobject NewShort(JEnv env, jshort value);
		static jobject NewInt(JEnv env, jint value);
		static jobject NewLong(JEnv env, jlong value);
		static jobject NewFloat(JEnv env, jfloat value);
		static jobject NewDouble(JEnv env, jdouble value);

		static jbyte ByteValue(JEnv env, jobject value);
		static jchar CharValue(JEnv env, jobject value);
		static jboolean BooleanValue(JEnv env, jobject value);
		static jshort ShortValue(JEnv env, jobject value);
		static jint IntValue(JEnv env, jobject value);
		static jlong LongValue(JEnv env, jobject value);
		static jfloat FloatValue(JEnv env, jobject value);
		static jdouble DoubleValue(JEnv env, jobject value);

		static Type getClassType(int retType);

	private:
		struct PrimitiveTypeInfo
		{
			PrimitiveTypeInfo()
				: clazz(nullptr), ctorID(nullptr), fieldID(nullptr)
			{
			}

			jclass clazz;
			jmethodID ctorID;
			jfieldID fieldID;

			template<int typeID>
			static PrimitiveTypeInfo& GetPrimitiveTypeInfo();

		private:

			static PrimitiveTypeInfo s_primitiveTypeCache[static_cast<int>(Type::PrimitiveTypeCount)];
		};
	};
}

#endif /* JNIPRIMITIVETYPE_H_ */
