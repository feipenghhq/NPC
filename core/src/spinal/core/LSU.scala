/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
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

    val byteAddr = io.addr(1 downto 0)

    // ---------------------------------------
    // Write Logic
    // ---------------------------------------
    // aw
    io.dbus.awReq(io.memWrite)
    io.dbus.aw.payload.awaddr := io.addr

    // b channel is always ready
    io.dbus.b.ready := True

    // w
    io.dbus.wReq(io.memWrite)
    switch(io.opcode(1 downto 0)) {
        is(0) { // SB
            io.dbus.w.payload.wstrb := B(1, config.nbyte bits) |<< byteAddr
            io.dbus.w.payload.wdata := io.wdata(7 downto 0) #* 4
        }
        is(1) { // SH
            io.dbus.w.payload.wstrb := byteAddr(1) ## byteAddr(1) ## ~byteAddr(1) ## ~byteAddr(1)
            io.dbus.w.payload.wdata := io.wdata(15 downto 0) #* 2
        }
        default { // SW
            io.dbus.w.payload.wstrb := ((1 << config.nbyte) - 1)
            io.dbus.w.payload.wdata := io.wdata
        }
    }

    io.wready := io.dbus.b.fire

    // ---------------------------------------
    // Read logic
    // ---------------------------------------
    // ar
    io.dbus.arReq(io.memRead)
    io.dbus.ar.payload.araddr := io.addr

    // r
    io.dbus.r.ready := True
    // select the correct data portion based on the address
    val byteData = io.dbus.r.payload.rdata.subdivideIn(8 bits).read(byteAddr(1 downto 0))
    val halfData = io.dbus.r.payload.rdata.subdivideIn(16 bits).read(byteAddr(1 downto 1))
    switch(io.opcode(2 downto 0)) {
        is(0) { // LB
            io.rdata := byteData.asSInt.resize(config.xlen bits).asBits // sign extension
        }
        is(1) { // LH
            io.rdata := halfData.asSInt.resize(config.xlen bits).asBits // sign extension
        }
        is(4) { // LBU
            io.rdata := byteData.resized
        }
        is(5) { // LHU
            io.rdata := halfData.resized
        }
        default { // LW
            io.rdata := io.dbus.r.payload.rdata
        }
    }
    io.rvalid := io.dbus.r.fire
}


object LSUVerilog extends App {
    val config = RiscCoreConfig(32, 0x00000000, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(LSU(config))
}