/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
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

case class Axi4LiteSramDpi (config: RiscCoreConfig) extends Component {
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
    sram.io.valid   := io.bus.ar.valid | io.bus.aw.valid
    sram.io.write   := io.bus.aw.valid
    sram.io.addr    := io.bus.ar.valid ? io.bus.ar.payload.araddr | io.bus.aw.payload.awaddr
    sram.io.strobe  := io.bus.w.payload.wstrb
    sram.io.wdata   := io.bus.w.payload.wdata


    io.bus.ar.ready := True
    io.bus.aw.ready := True

    io.bus.b.valid := True
    io.bus.b.payload.bresp := 0

    io.bus.r.valid := RegNext(io.bus.ar.valid) init False // there is 1 fixed read latency
    io.bus.r.payload.rdata := sram.io.rdata
}

