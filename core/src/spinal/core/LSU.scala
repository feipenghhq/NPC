/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 6/11/2024
 *
 * ------------------------------------------------------------------------------------------------
 * LSU: Load Store Unit
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._
import _root_.bus.Axi4Lite.Axi4Lite

case class LSU(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val dbus = master(Axi4Lite(config.axi4LiteConfig))
        val memRead = in port Bool()
        val memWrite = in port Bool()
        val opcode = in port Bits(3 bits)
        val addr = in port config.xlenUInt
        val wdata = in port config.xlenBits
        val rdata = out port config.xlenBits
        val wready = out port Bool()
        val rvalid = out port Bool()
    }
    noIoPrefix()

    // In some design the AXI bus width might be configured to 64 bits while the CPU core
    // is 32 bit wide. The logic here still works for this case.
    val dataWidth = config.axi4LiteConfig.dataWidth
    val _64bit = dataWidth == 64
    val numByte = dataWidth / 8
    val numHalf = dataWidth / 16
    val numWord = dataWidth / 32

    val selAddr = if (_64bit) io.addr(2 downto 0) else io.addr(1 downto 0)

    // ---------------------------------------
    // Write Logic
    // ---------------------------------------
    // aw channel
    io.dbus.awReq(io.memWrite)
    val aw = io.dbus.aw.payload
    aw.awaddr  := io.addr
    aw.awid    := 0
    aw.awlen   := 0
    aw.awburst := 0
    switch(io.opcode(1 downto 0)) {
        is(0)   {aw.awsize := B"000"} // SB
        is(1)   {aw.awsize := B"001"} // SH
        default {aw.awsize := B"010"} // SW
    }

    // b channel
    io.dbus.b.ready := True

    // w channel
    io.dbus.wReq(io.memWrite)
    val w = io.dbus.w.payload
    w.wlast  := True
    // - wstrb can be obtains by using the byte offset (selAddr) to shift
    //   the mask to the correct position
    // - data is duplicated and placed in all the chunks. (For example, for store byte, the same byte
    //   is placed in all the 4 byte in the 4 byte wide data)
    switch(io.opcode(1 downto 0)) {
        is(0) { // SB
            w.wstrb := (B(1, w.wstrb.getWidth bits) |<< selAddr).resized
            w.wdata := (io.wdata(7 downto 0) #* numByte).resized
        }
        is(1) { // SH
            w.wstrb := (B(3, w.wstrb.getWidth bits) |<< selAddr).resized
            w.wdata := (io.wdata(15 downto 0) #* numHalf).resized
        }
        default { // SW
            w.wstrb := (B(15, w.wstrb.getWidth bits) |<< selAddr).resized
            w.wdata := (io.wdata #* numWord).resized
        }
    }

    io.wready := io.dbus.b.fire

    // ---------------------------------------
    // Read logic
    // ---------------------------------------
    // ar channel
    io.dbus.arReq(io.memRead)
    val ar = io.dbus.ar.payload
    ar.araddr  := io.addr
    ar.arid    := 0
    ar.arlen   := 0
    ar.arburst := 0
    switch(io.opcode(2 downto 0)) {
        is(0, 4)   {ar.arsize := B"000"} // LB/LBU
        is(1, 5)   {ar.arsize := B"001"} // LH/LHU
        default    {ar.arsize := B"010"} // LW
    }

    // r channel
    io.dbus.r.ready := True
    // select the correct data portion based on the address
    val byteData = io.dbus.r.payload.rdata.subdivideIn(8 bits).read(selAddr(selAddr.getWidth-1 downto 0))
    val halfData = io.dbus.r.payload.rdata.subdivideIn(16 bits).read(selAddr(selAddr.getWidth-1 downto 1))
    val wordData = if (_64bit) io.dbus.r.payload.rdata.subdivideIn(32 bits).read(selAddr(selAddr.getWidth-1 downto 2))
                   else        io.dbus.r.payload.rdata
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
