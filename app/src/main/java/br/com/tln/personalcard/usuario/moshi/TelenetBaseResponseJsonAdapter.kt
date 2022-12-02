package br.com.tln.personalcard.usuario.moshi

import br.com.tln.personalcard.usuario.webservice.response.TelenetBaseResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class TelenetBaseResponseJsonAdapter<T>(val moshi: Moshi, resultsType: Type) :
    JsonAdapter<TelenetBaseResponse<T>>() {

    private val options: JsonReader.Options = JsonReader.Options.of("status", "results")

    private val intAdapter: JsonAdapter<Int> =
        moshi.adapter<Int>(Int::class.java, kotlin.collections.emptySet(), "status")
    private val tNullableResultsAdapter: JsonAdapter<T> =
        moshi.adapter<T>(resultsType, kotlin.collections.emptySet(), "results")

    override fun toJson(writer: JsonWriter, value: TelenetBaseResponse<T>?) {
        if (value == null) {
            throw NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginObject()
        writer.name("status")
        intAdapter.toJson(writer, value.status)
        writer.name("results")
        tNullableResultsAdapter.toJson(writer, value.results)
        writer.endObject()
    }

    override fun fromJson(reader: JsonReader): TelenetBaseResponse<T>? {
        var status: Int? = null
        var results: T? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> status = reader.nextInt()
                1 -> results = tNullableResultsAdapter.fromJson(reader) ?: throw JsonDataException("Non-null value 'results' was null at ${reader.path}")
                -1 -> {
                    // Unknown name, skip it.
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()

        val r1 = status ?: throw JsonDataException("Required property 'status' missing at ${reader.path}")
        val r2 = results ?: throw JsonDataException("Required property 'results' missing at ${reader.path}")

        return TelenetBaseResponse(status = r1, results = r2)
    }

    override fun toString(): String = "GeneratedJsonAdapter(TelenetBaseResponse)"

    class Factory : JsonAdapter.Factory {
        override fun create(
            type: Type,
            annotations: MutableSet<out Annotation>,
            moshi: Moshi
        ): JsonAdapter<*>? {
            val rawType = Types.getRawType(type)

            if (rawType == TelenetBaseResponse::class.java && type is ParameterizedType) {
                val subType = type.actualTypeArguments.first()

                return TelenetBaseResponseJsonAdapter<Any>(moshi = moshi, resultsType = subType)
            }

            return null
        }
    }
}