/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 6/11/2024
 *
 * ------------------------------------------------------------------------------------------------
 * LSU: Load Store Unit
 * Currently the LSU support only Multiple Cycle CPU
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._
import _root_.bus.Axi4Lite.Axi4Lite

case class LSU(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val dbus     = master(Axi4Lite(config.axi4LiteConfig))
        val memRead  = in port Bool()
        val memWrite = in port Bool()
        val opcode   = in port Bits(3 bits)
        val addr     = in port config.xlenUInt
        val wdata    = in port config.xlenBits
        val rdata    = out port config.xlenBits
        val wready   = out port Bool()
        val rvalid   = out port Bool()
    }
    noIoPrefix()

    val numByte = config.xlen / 8
    val numHalf = config.xlen / 16
    val numWord = config.xlen / 32

    val selAddr = io.addr(1 downto 0)

    // ---------------------------------------
    // Write Logic
    // ---------------------------------------

    // Notes:
    // 1. AW and W channel are synchronized meaning awvalid and wvalid assert at the same cycle
    // 2. AW and W channel can handle different back-pressure (different awready and wready)

    // aw channel
    val aw = io.dbus.aw.payload
    // register the AXI valid instead of using combo logic this will create addition clock latency
    // but meet AXI requirement and achieve better timing
    val awPending = RegNextWhen(True, io.dbus.aw.fire) clearWhen(io.dbus.b.fire) init False
    // awPending make that sure we don't assert valid again when AW handshake complete.
    val awvalid = RegNextWhen(True, io.memWrite & ~awPending) clearWhen(io.dbus.aw.fire) init False
    io.dbus.aw.valid := awvalid
    aw.awaddr := io.addr

    // w channel
    val w = io.dbus.w.payload
    val wPending = RegNextWhen(True, io.dbus.w.fire) clearWhen(io.dbus.b.fire) init False
    val wvalid = RegNextWhen(True, io.memWrite & ~wPending) clearWhen(io.dbus.w.fire) init False
    io.dbus.w.valid := wvalid
    // wstrb can be obtains by using the byte offset (selAddr) to shift the mask to the correct position
    // data is duplicated and placed in all the chunks
    switch(io.opcode(1 downto 0)) {
        is(0) { // SB
            w.wstrb := B(1, w.wstrb.getWidth bits) |<< selAddr
            w.wdata := io.wdata(7 downto 0) #* numByte
        }
        is(1) { // SH
            w.wstrb := B(3, w.wstrb.getWidth bits) |<< selAddr
            w.wdata := io.wdata(15 downto 0) #* numHalf
        }
        default { // SW
            w.wstrb := B(15, w.wstrb.getWidth bits) |<< selAddr
            w.wdata := io.wdata #* numWord
        }
    }


    // b channel
    // we can move forward when the write response comes back
    // and for now, we just ignore the write response result and treat write as always successful
    io.wready := io.dbus.b.fire

    // IFU is always capable of consuming the write response
    if (config.lsuBreadyDelay == 0) {
        io.dbus.b.ready := True
    }
    // Add delay to test back-pressure on axi bus
    else {
        val bready = Timeout(config.lsuBreadyDelay)
        when(io.dbus.aw.fire) {bready.clear()}
        io.dbus.b.ready := bready
    }

    // ---------------------------------------
    // Read logic
    // ---------------------------------------
    // ar channel
    val ar = io.dbus.ar.payload
    val arPending = RegNextWhen(True, io.dbus.ar.fire) clearWhen(io.dbus.r.fire) init False
    val arvalid = RegNext(io.memRead & ~arPending) init False
    io.dbus.ar.valid := arvalid
    ar.araddr := io.addr

    // r channel
    // IFU is always capable of consuming the write response
    if (config.lsuRreadyDelay == 0) {
        io.dbus.r.ready := True
    }
    // Add delay to test back-pressure on axi bus
    else {
        val rready = Timeout(config.lsuRreadyDelay)
        when(io.dbus.ar.fire) {rready.clear()}
        io.dbus.r.ready := rready
    }
    // select the correct data portion based on the address
    val byteData = io.dbus.r.payload.rdata.subdivideIn(8 bits).read(selAddr(selAddr.getWidth-1 downto 0))
    val halfData = io.dbus.r.payload.rdata.subdivideIn(16 bits).read(selAddr(selAddr.getWidth-1 downto 1))
    val wordData = io.dbus.r.payload.rdata
    switch(io.opcode(2 downto 0)) {
        is(0) { // LB
            io.rdata := byteData.asSInt.resize(config.xlen bits).asBits // signed extension
        }
        is(1) { // LH
            io.rdata := halfData.asSInt.resize(config.xlen bits).asBits // signed extension
        }
        is(4) { // LBU
            io.rdata := byteData.resized // unsigned extension
        }
        is(5) { // LHU
            io.rdata := halfData.resized // unsigned extension
        }
        default { // LW
            io.rdata := wordData.resized
        }
    }
    io.rvalid := io.dbus.r.fire
}
