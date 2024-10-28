/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 6/23/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Axi4Lite Bus Interface
 * ------------------------------------------------------------------------------------------------
 */

package bus.Axi4Lite

import spinal.core._
import spinal.lib._

case class Axi4LiteConfig(
    addrWidth: Int = 32,    // Address width
    dataWidth: Int = 32,    // Data width
    outstanding: Int = 1    // number of supported outstanding transaction
) {
    def AW = addrWidth
    def DW = dataWidth
}

case class Axi4LiteAr(config: Axi4LiteConfig) extends Bundle {
    val araddr = UInt(config.AW bits)
    val arprot = Bits(3 bits)
}

case class Axi4LiteR(config: Axi4LiteConfig) extends Bundle {
    val rdata = Bits(config.DW bits)
    val rresp = Bits(2 bits)
}

case class Axi4LiteAw(config: Axi4LiteConfig) extends Bundle {
    val awaddr = UInt(config.AW bits)
    val awprot = Bits(3 bits)
}

case class Axi4LiteW(config: Axi4LiteConfig) extends Bundle {
    val wdata = Bits(config.DW bits)
    val wstrb = Bits(config.DW/8 bits)
}

case class Axi4LiteB(config: Axi4LiteConfig) extends Bundle {
    val bresp = Bits(2 bits)
}

case class Axi4Lite(config: Axi4LiteConfig) extends Bundle with IMasterSlave {
    val ar = Stream(Axi4LiteAr(config))
    val r  = Stream(Axi4LiteR(config))
    val aw = Stream(Axi4LiteAw(config))
    val w  = Stream(Axi4LiteW(config))
    val b  = Stream(Axi4LiteB(config))

    def asMaster() {
        master(ar, aw, w)
        slave(r, b)
    }

    /**
      * Functions to help rename the AXI signals in the generated verilog code
      *
      * @param prefix the prefix name to the AXI signal
      */
    def updateSignalName(prefix: String): Axi4Lite = {
        def setName[T<:Bundle](channel: => Stream[T]) {
            channel.valid.setName(prefix + "_" + channel.name + "valid")
            channel.ready.setName(prefix + "_" + channel.name + "ready")
            channel.payload.elements.foreach(f => f._2.setName(prefix + "_" + f._1))
        }
        setName(ar)
        setName(r)
        setName(aw)
        setName(w)
        setName(b)
        this
    }
}