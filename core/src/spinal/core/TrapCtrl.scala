/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/14/2024
 *
 * ------------------------------------------------------------------------------------------------
 * TrapCtrl: Exception and Interrupt
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._

case class TrapCtrl(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val csrRdPort = CsrRdPort(config).flip()
        val csrWrPort = CsrWrPort(config).flip()
        val ecall = in port Bool()
        val mret = in port Bool()
        val pc = in port config.xlenUInt
        val trapCtrl = master Flow(config.xlenUInt)
        val trap = out port Bool()
    }
    noIoPrefix()

    // -------------------------------------
    // Entering trap
    // -------------------------------------

    // update mepc register
    io.csrWrPort.mepc := io.pc.asBits

    // update mcause register
    io.csrWrPort.mcause(config.xlen-1) := False // No interrupt support yet
    io.csrWrPort.mcause(config.xlen-2 downto 0) := 11 // only support m-ecall for now

    // Set trap PC
    val trapEntPC = io.csrRdPort.mtvec(config.xlen-1 downto 2) ## B(0, 2 bits)

    // -------------------------------------
    // Exit trap
    // -------------------------------------
    val trapExtPC = io.csrRdPort.mepc

    // -----------------------------------
    // Final control signal
    // -----------------------------------
    io.trapCtrl.valid := io.mret | io.ecall
    io.trapCtrl.payload := Mux(io.mret, trapExtPC, trapEntPC).asUInt
    io.trap := io.ecall

}

object TrapCtrlVerilog extends App {
    val config = RiscCoreConfig(32, 0x00000000, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(TrapCtrl(config)).printPruned()
}