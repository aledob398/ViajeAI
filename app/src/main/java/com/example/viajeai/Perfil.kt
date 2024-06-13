import android.os.Parcel
import android.os.Parcelable

data class Perfil(
    val idUsuario: String = "",
    var nombre: String = "",
    val preferencias: List<Preferencia> = emptyList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        mutableListOf<Preferencia>().apply {
            parcel.readList(this, Preferencia::class.java.classLoader)
        }
    )


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idUsuario)
        parcel.writeString(nombre)
        parcel.writeList(preferencias as List<*>?)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Perfil> {
        override fun createFromParcel(parcel: Parcel): Perfil {
            return Perfil(parcel)
        }

        override fun newArray(size: Int): Array<Perfil?> {
            return arrayOfNulls(size)
        }
    }
}


enum class Preferencia {
    MUSEOS,
    TEATROS,
    DEPORTES_EXTREMOS,
    CONCIERTOS,
    DEPORTES,
    ACTIVIDADES_AL_AIRE_LIBRE,
    SITIOS_HISTORICOS,
    MONUMENTOS,
    PLAYA,
    MONTAÑA,
    PARQUES_NATURALES,
    PARQUES_DE_ATRACCIONES,
    VIDA_NOCTURNA
}
