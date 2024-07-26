/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/8/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Register File
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._
import spinal.core.Verilator.public

case class RdWrCtrl(config: RiscCoreConfig) extends Bundle {
    val addr  = config.regidUInt
    val data  = config.xlenBits
}

case class RegisterFile(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val rs1Addr = in port UInt(config.regidWidth bits)
        val rs1Data = out port config.xlenBits
        val rs2Addr = in port UInt(config.regidWidth bits)
        val rs2Data = out port config.xlenBits
        val rdWrCtrl = slave Flow(RdWrCtrl(config))
    }
    noIoPrefix()

    val rdWrCtrl = io.rdWrCtrl.payload

    val regs = Mem(config.xlenBits, config.nreg)
    regs.setName("regs")
    regs.addAttribute(public)

    regs.write(
        address = io.rdWrCtrl.payload.addr,
        data = io.rdWrCtrl.payload.data,
        enable = io.rdWrCtrl.valid)

    io.rs1Data := Mux(io.rs1Addr === 0, B(0, config.xlen bits), regs.readAsync(io.rs1Addr))
    io.rs2Data := Mux(io.rs2Addr === 0, B(0, config.xlen bits), regs.readAsync(io.rs2Addr))
}
