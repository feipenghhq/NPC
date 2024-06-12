/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/11/2024
 *
 * ------------------------------------------------------------------------------------------------
 * MEU: Memory Execution Unit
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._


case class DbusBundle(config: RiscCoreConfig) extends Bundle with IMasterSlave {
    val addr = config.xlenUInt
    val valid = Bool()
    val write = Bool()
    val strobe = Bits(config.nbyte bits)
    val wdata = config.xlenBits
    val rdata = config.xlenBits

    override def asMaster(): Unit = {
        out(addr, valid, write, strobe, wdata)
        in(rdata)
    }
}

case class MEU(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val dbus = master(DbusBundle(config))
        val memRead = in port Bool()
        val memWrite = in port Bool()
        val opcode = in port Bits(5 bits)
        val addr = in port config.xlenUInt
        val wdata = in port config.xlenBits
        val rdata = out port config.xlenBits
    }

    val byteAddr = io.addr(1 downto 0)

    // ---------------------------------------
    // Generate control signal
    // ---------------------------------------
    io.dbus.valid := io.memRead | io.memWrite
    io.dbus.write := io.memWrite
    io.dbus.addr := io.addr

    // ---------------------------------------
    // Generate Write data
    // ---------------------------------------

    switch(io.opcode(1 downto 0)) {
        is(0) { // SB
            io.dbus.strobe := B(1, config.nbyte bits) |<< byteAddr
            io.dbus.wdata := io.wdata(7 downto 0) #* 4
        }
        is(1) { // SH
            io.dbus.strobe := byteAddr(1) ## byteAddr(1) ## ~byteAddr(1) ## ~byteAddr(1)
            io.dbus.wdata := io.wdata(15 downto 0) #* 2
        }
        default { // SW
            io.dbus.strobe := (1 << config.nbyte - 1)
            io.dbus.wdata := io.wdata
        }
    }

    // ---------------------------------------
    // Processing read data
    // ---------------------------------------

    // select the correct data portion based on the address
    val byteData = io.dbus.rdata.subdivideIn(8 bits).read(byteAddr(1 downto 0))
    val halfData = io.dbus.rdata.subdivideIn(16 bits).read(byteAddr(1 downto 1))
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
            io.rdata := io.dbus.rdata
        }
    }
}


object MEUVerilog extends App {
    val config = RiscCoreConfig(32, 0x00000000, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(MEU(config))
}