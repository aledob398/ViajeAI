import android.os.Parcel
import android.os.Parcelable

data class Viaje(

    var idUsuario: String? = null,
    var destino: String? = null,
    var presupuesto: Double? = null,
    var duracion: Int? = null,
    var actividades: List<String>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.createStringArrayList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idUsuario)
        parcel.writeString(destino)
        parcel.writeValue(presupuesto)
        parcel.writeValue(duracion)
        parcel.writeStringList(actividades)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Viaje> {
        override fun createFromParcel(parcel: Parcel): Viaje {
            return Viaje(parcel)
        }

        override fun newArray(size: Int): Array<Viaje?> {
            return arrayOfNulls(size)
        }
    }
}
