/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 6/22/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Axi4Lite Sram: a wrapper on top of SramDpi
 * ------------------------------------------------------------------------------------------------
 */

package misc

import spinal.core._
import spinal.lib._
import config._
import _root_.bus.Axi4Lite._

case class Axi4LiteSramDpi(config: RiscCoreConfig, respDelay: Int = 1, reqDelay: Int = 0) extends Component {
    val io = new Bundle {
        val bus = slave(Axi4Lite(config.axi4LiteConfig))
        val pc = in port config.xlenUInt
        val ifetch = in port Bool()
    }

    val sram = SramDpi(config)
    sram.io.ifetch := io.ifetch
    sram.io.pc := io.pc

    // For simplicity, we assume that aw and w channel arrives at the same time.
    // our cpu design make sure this is the case
    sram.io.valid   := io.bus.ar.fire | io.bus.aw.fire
    sram.io.write   := io.bus.aw.fire
    sram.io.addr    := io.bus.ar.valid ? io.bus.ar.payload.araddr | io.bus.aw.payload.awaddr
    sram.io.strobe  := io.bus.w.payload.wstrb.resized
    sram.io.wdata   := io.bus.w.payload.wdata.resized

    // Add some request delay to test if the CPU can handle the back pressure correctly
    if (reqDelay > 0) {
        def readySet[T <: Bundle](name: String, ch: Stream[T]): Timeout = {
            val to = Timeout(reqDelay) clearWhen(ch.fire | ~ch.valid) setName(name)
            to
        }
        io.bus.ar.ready := readySet("arreadyTO", io.bus.ar)
        io.bus.aw.ready := readySet("awreadyTO", io.bus.aw)
        io.bus.w.ready  := readySet("wreadyTO",  io.bus.w)
    }
    // else always set the ready to 1
    else {
        io.bus.ar.ready := True
        io.bus.aw.ready := True
        io.bus.w.ready  := True
    }

    // write response
    io.bus.b.valid := io.bus.aw.fire // sent the write response when we receives the write req/data
    io.bus.b.payload.bresp := 0
    io.bus.b.payload.bid := 0

    // read response
    io.bus.r.valid := Delay(io.bus.ar.fire, respDelay)
    io.bus.r.payload.rid   := 0
    io.bus.r.payload.rlast := True
    io.bus.r.payload.rresp := 0
    // there is already 1 fixed read latency in the SramDpi module due to synchronous ram
    require(respDelay >= 1)
    if (respDelay == 1) {
        io.bus.r.payload.rdata := sram.io.rdata.resized
    // For now, only support 2 read response delay
    } else {
        io.bus.r.payload.rdata := RegNextWhen(sram.io.rdata, RegNext(sram.io.valid) init False).resized
    }

    // assertion to make sure that aw and w arrives at the same time
    assert(
      assertion = ~(io.bus.aw.fire ^ io.bus.w.fire),
      message   = "aw and w channel should arrive at the same time",
      severity  = ERROR
    )

}

