package co.thecodewarrior.unifontcli.common

class Vec2d(val x: Double, val y: Double) {

    @Transient
    val xf: Float = x.toFloat()
    @Transient
    val yf: Float = y.toFloat()
    @Transient
    val xi: Int = Math.floor(x).toInt()
    @Transient
    val yi: Int = Math.floor(y).toInt()

    fun floor(): Vec2d {
        return Vec2d(Math.floor(x), Math.floor(y))
    }

    fun ceil(): Vec2d {
        return Vec2d(Math.ceil(x), Math.ceil(y))
    }

    fun setX(value: Double): Vec2d {
        return Vec2d(value, y)
    }

    fun setY(value: Double): Vec2d {
        return Vec2d(x, value)
    }

    fun add(other: Vec2d): Vec2d {
        return Vec2d(x + other.x, y + other.y)
    }

    fun add(otherX: Double, otherY: Double): Vec2d {
        return Vec2d(x + otherX, y + otherY)
    }

    fun sub(other: Vec2d): Vec2d {
        return Vec2d(x - other.x, y - other.y)
    }

    fun sub(otherX: Double, otherY: Double): Vec2d {
        return Vec2d(x - otherX, y - otherY)
    }

    fun mul(other: Vec2d): Vec2d {
        return Vec2d(x * other.x, y * other.y)
    }

    fun mul(otherX: Double, otherY: Double): Vec2d {
        return Vec2d(x * otherX, y * otherY)
    }

    fun mul(amount: Double): Vec2d {
        return Vec2d(x * amount, y * amount)
    }

    fun divide(other: Vec2d): Vec2d {
        return Vec2d(x / other.x, y / other.y)
    }

    fun divide(otherX: Double, otherY: Double): Vec2d {
        return Vec2d(x / otherX, y / otherY)
    }

    fun divide(amount: Double): Vec2d {
        return Vec2d(x / amount, y / amount)
    }

    infix fun dot(point: Vec2d): Double {
        return x * point.x + y * point.y
    }

    @delegate:Transient
    private val len by lazy { Math.sqrt(x * x + y * y) }

    fun length(): Double {
        return len
    }

    fun normalize(): Vec2d {
        val norm = length()
        return Vec2d(x / norm, y / norm)
    }

    fun squareDist(vec: Vec2d): Double {
        val d0 = vec.x - x
        val d1 = vec.y - y
        return d0 * d0 + d1 * d1
    }

    fun projectOnTo(other: Vec2d): Vec2d {
        val norm = other.normalize()
        return norm.mul(this.dot(norm))
    }

    fun rotate(theta: Number): Vec2d {
        val cs = Math.cos(theta.toDouble())
        val sn = Math.sin(theta.toDouble())
        return Vec2d(
                this.x * cs - this.y * sn,
                this.x * sn + this.y * cs
        )
    }

    //=============================================================================

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        var temp: Long
        temp = java.lang.Double.doubleToLongBits(x)
        result = prime * result + (temp xor temp.ushr(32)).toInt()
        temp = java.lang.Double.doubleToLongBits(y)
        result = prime * result + (temp xor temp.ushr(32)).toInt()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (javaClass != other.javaClass)
            return false
        return x == (other as Vec2d).x && y == other.y
    }

    override fun toString(): String {
        return "($x,$y)"
    }

    companion object {

        @JvmField
        val ZERO = Vec2d(0.0, 0.0)
        @JvmField
        val INFINITY = Vec2d(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
        @JvmField
        val NEG_INFINITY = Vec2d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)

        @JvmField
        val ONE = Vec2d(1.0, 1.0)
        @JvmField
        val X = Vec2d(1.0, 0.0)
        @JvmField
        val Y = Vec2d(0.0, 1.0)
        @JvmField
        val X_INFINITY = Vec2d(Double.POSITIVE_INFINITY, 0.0)
        @JvmField
        val Y_INFINITY = Vec2d(0.0, Double.POSITIVE_INFINITY)

        @JvmField
        val NEG_ONE = Vec2d(-1.0, -1.0)
        @JvmField
        val NEG_X = Vec2d(-1.0, 0.0)
        @JvmField
        val NEG_Y = Vec2d(0.0, -1.0)
        @JvmField
        val NEG_X_INFINITY = Vec2d(Double.NEGATIVE_INFINITY, 0.0)
        @JvmField
        val NEG_Y_INFINITY = Vec2d(0.0, Double.NEGATIVE_INFINITY)

        @JvmStatic
        fun min(a: Vec2d, b: Vec2d): Vec2d {
            return Vec2d(Math.min(a.x, b.x), Math.min(a.y, b.y))
        }

        @JvmStatic
        fun max(a: Vec2d, b: Vec2d): Vec2d {
            return Vec2d(Math.max(a.x, b.x), Math.max(a.y, b.y))
        }
    }
}

fun vec(x: Number, y: Number) = Vec2d(x.toDouble(), y.toDouble())

operator fun Vec2d.times(other: Vec2d) = this.mul(other)
operator fun Vec2d.times(other: Double) = this.mul(other)
operator fun Vec2d.times(other: Float) = this * other.toDouble()
operator fun Vec2d.times(other: Int) = this * other.toDouble()

operator fun Vec2d.div(other: Vec2d) = this.divide(other)
operator fun Vec2d.div(other: Double) = this.divide(other)
operator fun Vec2d.div(other: Float) = this / other.toDouble()
operator fun Vec2d.div(other: Int) = this / other.toDouble()

operator fun Vec2d.plus(other: Vec2d) = this.add(other)
operator fun Vec2d.minus(other: Vec2d) = this.sub(other)
operator fun Vec2d.unaryMinus() = this * -1

operator fun Vec2d.component1() = x
operator fun Vec2d.component2() = y
