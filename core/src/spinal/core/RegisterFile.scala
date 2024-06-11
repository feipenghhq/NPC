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

case class RdWrCtrl(config: RiscCoreConfig) extends Bundle {
    val addr  = in port config.regidUInt
    val data  = in port config.xlenBits
}

case class RegisterFile(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val rs1Addr = in port UInt(config.regidWidth bits)
        val rs1Data = out port config.xlenBits
        val rs2Addr = in port UInt(config.regidWidth bits)
        val rs2Data = out port config.xlenBits
        val rdWrCtrl = slave Flow(RdWrCtrl(config))
    }

    val register = Mem(config.xlenBits, config.nreg)

    register.write(
        address = io.rdWrCtrl.payload.addr,
        data = io.rdWrCtrl.payload.data,
        enable = io.rdWrCtrl.valid)

    io.rs1Data := register.readAsync(io.rs1Addr)
    io.rs2Data := register.readAsync(io.rs2Addr)
}

object RegisterFile {
    def apply(config: RiscCoreConfig, cpuCtrl: CpuCtrl, rdWrCtrl: Flow[RdWrCtrl], rs1Data: Bits, rs2Data: Bits): RegisterFile = {
        val rf = RegisterFile(config)
        rf.io.rs1Addr := cpuCtrl.rs1Addr
        rf.io.rs2Addr := cpuCtrl.rs2Addr
        rf.io.rdWrCtrl <> rdWrCtrl
        rf.io.rs1Data <> rs1Data
        rf.io.rs2Data <> rs2Data
        rf
    }
}

object RegisterFileVerilog extends App {
    val config = RiscCoreConfig(32, 0x00000000, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(RegisterFile(config))
}
