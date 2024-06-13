import android.os.Parcel
import android.os.Parcelable

data class Alojamiento(
    val descripcion: String,
    val name: String,
    val email: String,
    val phone: String,
    val web: String,
    val precio: Double,
    val imageUrl: String,
    val latitude: Double,
    val longitude: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(descripcion)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(phone)
        parcel.writeString(web)
        parcel.writeDouble(precio)
        parcel.writeString(imageUrl)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Alojamiento> {
        override fun createFromParcel(parcel: Parcel): Alojamiento {
            return Alojamiento(parcel)
        }

        override fun newArray(size: Int): Array<Alojamiento?> {
            return arrayOfNulls(size)
        }
    }
}
