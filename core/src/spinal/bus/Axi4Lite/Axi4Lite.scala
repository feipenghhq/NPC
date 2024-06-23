package bus.Axi4Lite

import spinal.core._
import spinal.lib._

case class Axi4LiteConfig(
    addrWidth: Int = 32,
    dataWidth: Int = 32,
)

case class Axi4LiteAr(config: Axi4LiteConfig) extends Bundle {
    val araddr = UInt(config.addrWidth bits)
}

case class Axi4LiteR(config: Axi4LiteConfig) extends Bundle {
    val rdata = Bits(config.dataWidth bits)
    val rresp = Bits(2 bits)
}

case class Axi4LiteAw(config: Axi4LiteConfig) extends Bundle {
    val awaddr = UInt(config.addrWidth bits)
}

case class Axi4LiteW(config: Axi4LiteConfig) extends Bundle {
    val wdata = Bits(config.dataWidth bits)
    val wstrb = Bits(config.dataWidth/8 bits)
}

case class Axi4LiteB(config: Axi4LiteConfig) extends Bundle {
    val bresp = Bits(2 bits)
}

case class Axi4Lite(config: Axi4LiteConfig) extends Bundle with IMasterSlave {
    val ar = Stream(Axi4LiteAr(config))
    val r = Stream(Axi4LiteR(config))
    val aw = Stream(Axi4LiteAw(config))
    val w = Stream(Axi4LiteW(config))
    val b = Stream(Axi4LiteB(config))

    def asMaster() {
        master(ar, aw, w)
        slave(r, b)
    }
}