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
    addrWidth: Int = 32,
    dataWidth: Int = 32,
    axi4:      Boolean = false  // Use full AXI4 signal, but only supports the AXI4-Lite protocol and burst is not supported
                                // This is mainly used for byte enable control in write request.
)

case class Axi4LiteAr(config: Axi4LiteConfig) extends Bundle {
    val araddr  = UInt(config.addrWidth bits)
    val arid    = config.axi4 generate UInt(4 bits)
    val arlen   = config.axi4 generate UInt(8 bits)
    val arsize  = config.axi4 generate Bits(3 bits)
    val arburst = config.axi4 generate Bits(2 bits)
}

case class Axi4LiteR(config: Axi4LiteConfig) extends Bundle {
    val rdata = Bits(config.dataWidth bits)
    val rresp = Bits(2 bits)
    val rlast = config.axi4 generate Bool()
    val rid   = config.axi4 generate UInt(4 bits)
}

case class Axi4LiteAw(config: Axi4LiteConfig) extends Bundle {
    val awaddr  = UInt(config.addrWidth bits)
    val awid    = config.axi4 generate UInt(4 bits)
    val awlen   = config.axi4 generate UInt(8 bits)
    val awsize  = config.axi4 generate Bits(3 bits)
    val awburst = config.axi4 generate Bits(2 bits)
}

case class Axi4LiteW(config: Axi4LiteConfig) extends Bundle {
    val wdata = Bits(config.dataWidth bits)
    val wstrb = Bits(config.dataWidth/8 bits)
    val wlast = config.axi4 generate Bool
}

case class Axi4LiteB(config: Axi4LiteConfig) extends Bundle {
    val bresp = Bits(2 bits)
    val bid   = config.axi4 generate UInt(4 bits)
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
      * Functions to help assert valid signal for the req channel (ar/aw) from other conditions.
      * It will assert valid till the valid-ready handshake complete in the req channel.
      * Then it will stops asserting valid till the response (r/b) comes back.
      * This logic can be used for a request that keeps asserting until the data/response comes back as
      * you don't want to assert valid again after the ar/aw channel handshake completes for the same request.
      *
      * @param set The logic to set the valid signal
      * @param reqChannel request channel (ar/aw) instance
      * @param respChannel response channel (r/b) instance
      */
    private def _request[T <: Bundle, K <: Bundle](set: Bool, reqChannel: Stream[T], respChannel: Stream[K]) {
        // An indicator to tell us that we have sent the request and waiting for the response
        val sent = RegNextWhen(True, reqChannel.fire) init False setName(reqChannel.name + "Sent")
        sent.clearWhen(respChannel.fire)
        reqChannel.valid := set & ~sent
    }

    def arReq(set: => Bool) {
        _request(set, ar, r)
    }

    def awReq(set: => Bool ) {
        _request(set, aw, b)
    }

    def wReq(set: => Bool ) {
        _request(set, w, b)
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