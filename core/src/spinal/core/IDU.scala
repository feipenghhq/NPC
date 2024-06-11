/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/9/2024
 *
 * ------------------------------------------------------------------------------------------------
 * IDU: Instruction Decoder Unit
 * ------------------------------------------------------------------------------------------------
 * Instantiate the Decoder and Register File
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._

case class IduBundle(config: RiscCoreConfig) extends Bundle {
    val cpuCtrl = CpuCtrl(config)
    val rs1Data = config.xlenBits
    val rs2Data = config.xlenBits
}

case class IDU(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val ifuData = slave Stream(IfuBundle(config))
        val iduData = master Stream(IduBundle(config))
        val rdWrCtrl = slave Flow(RdWrCtrl(config))
    }

    val dec = Decoder(config, io.ifuData, io.iduData.cpuCtrl)

    val rf = RegisterFile(config, io.iduData.payload.cpuCtrl, io.rdWrCtrl,
        rs1Data = io.iduData.payload.rs1Data,
        rs2Data = io.iduData.payload.rs2Data)

    io.ifuData.ready := io.iduData.ready
    io.iduData.valid := io.ifuData.valid
}

object IDUVerilog extends App {
    val config = RiscCoreConfig(32, 0x00000000, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(IDU(config))
}