/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 6/22/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Axi4LiteRAM: RAM access by Axi4Lite Bus
 * ------------------------------------------------------------------------------------------------
 * 10/20/2024: Improved the design to better handle AXI handshake
 * With Axi4Lite bus, several issues needs to be addressed:
 * 1. How to synchronize the AW and W channel, they might comes at different cycle
 * 2. How to handle the case when there are both read and write request (this is possible for AXI)
 * 3. The AXI spec requires that all the interface signal to be flopped:
 *    (On master and slave interfaces there must be no combinatorial paths between input and output signals.)
 *    so it's need some careful consideration to maintain the good throughput while meeting the requirement
 *
 * To achieve a high performance design, we use skid buffer for all the 3 channels to handle above 3 cases.
 * This is probably not area optimized but it will achieve maximum throughput and meet AXI interface requirement.
 * This also give us a better timing since the dependence between ready-valid is cut
 * ------------------------------------------------------------------------------------------------
 */

package common

import spinal.core._
import spinal.lib._
import config._
import _root_.bus.Axi4Lite._

/**
  * Define different ram type
  */
sealed abstract class RamType
object RamType {
  case object DPI extends RamType       // RAM using Verilog DPI
}

/**
  * Axi4Lite RAM
  *
  * @param config Axi4Lite Config
  * @param ramType Ram type
  */
case class Axi4LiteRam(config: RiscCoreConfig, ramType: RamType) extends Component {

    val isDPI = ramType == RamType.DPI

    val io = new Bundle {
        val axi4l = slave(Axi4Lite(config.axi4LiteConfig))
        // tracing info for DPI
        val pc     = isDPI generate in port config.xlenUInt
        val ifetch = isDPI generate in port Bool()
    }

    // --------------------------------------------
    // skid buffer for all the 3 channel
    // --------------------------------------------

    val arSkidBuffer = SkidBuffer(io.axi4l.ar.payloadType)
    arSkidBuffer.io.in <> io.axi4l.ar
    // Go ahead to access the memory when the outgoing R channel is not stalled with a response
    arSkidBuffer.io.out.ready := ~(io.axi4l.r.valid & ~io.axi4l.r.ready)

    val awSkidBuffer = SkidBuffer(io.axi4l.aw.payloadType)
    val wSkidBuffer  = SkidBuffer(io.axi4l.w.payloadType)
    awSkidBuffer.io.in <> io.axi4l.aw
    wSkidBuffer.io.in  <> io.axi4l.w
    // Go ahead to access the memory when
    // 1. the outgoing B channel is not stalled with a response
    // 2. both the AW and W channels arrives
    // 3. no read request being take
    awSkidBuffer.io.out.ready := ~(io.axi4l.b.valid & ~io.axi4l.b.ready) &
                                 (awSkidBuffer.io.out.valid & wSkidBuffer.io.out.valid) &
                                 ~arSkidBuffer.io.out.fire
    wSkidBuffer.io.out.ready := awSkidBuffer.io.out.ready

    val rEnable = arSkidBuffer.io.out.fire
    val wEnable = awSkidBuffer.io.out.fire

    // --------------------------------------------
    //  Access the RAM
    // --------------------------------------------

    val rdata = config.xlenBits

    // RamType: DPI
    if (isDPI) {
        // instantiate the ram
        val ram = RamDpi(config)
        ram.io.ifetch := io.ifetch
        ram.io.pc     := io.pc

        ram.io.valid   := rEnable | wEnable
        ram.io.write   := wEnable
        ram.io.addr    := Mux(rEnable, arSkidBuffer.io.out.payload.araddr, awSkidBuffer.io.out.payload.awaddr)
        ram.io.strobe  := wSkidBuffer.io.out.payload.wstrb
        ram.io.wdata   := wSkidBuffer.io.out.payload.wdata
        rdata          := ram.io.rdata
    }

    // --------------------------------------------
    //  Response channel
    // --------------------------------------------

    // need a buffer to hold the read data in case of back-pressure of r channel
    val rdataValid = RegNext(rEnable) init False
    val rdataBuffer = RegNextWhen(rdata, rdataValid)
    io.axi4l.r.valid := RegNextWhen(True, rEnable) clearWhen(io.axi4l.r.fire)
    io.axi4l.r.payload.rdata := Mux(rdataValid, rdata, rdataBuffer)
    io.axi4l.r.payload.rresp := 0

    io.axi4l.b.valid := RegNextWhen(True, wEnable) clearWhen(io.axi4l.b.fire)
    io.axi4l.b.payload.bresp := 0
}


/**
  * Black box for verilog module RamDpi
  *
  * @param config
  */
case class RamDpi (config: RiscCoreConfig) extends BlackBox {

  val generic = new Generic {
    val XLEN = config.xlen
  }

  val io = new Bundle {
    val clk    = in port Bool()
    val rst_b  = in port Bool()
    val valid  = in port Bool()
    val write  = in port Bool()
    val addr   = in port config.xlenUInt
    val strobe = in port Bits(config.nbyte bits)
    val wdata  = in port config.xlenBits
    val rdata  = out port config.xlenBits
    val ifetch = in port Bool()
    val pc     = in port config.xlenUInt
  }

  noIoPrefix()
  mapClockDomain(clock = io.clk, reset = io.rst_b, resetActiveLevel = LOW)
}
