/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/10/2024
 *
 * ------------------------------------------------------------------------------------------------
 * BEU: Branch Execution Unit
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._

case class BEU(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val branch = in port Bool()
        val jump = in port Bool()
        val opcode = in port Bits(5 bits)
        val src1 = in port config.xlenBits
        val src2 = in port config.xlenBits
        val addr = in port config.xlenUInt // branch/jump target address comes from ALU
        val branchCtrl = master Flow(config.xlenUInt)
    }
    noIoPrefix()

    // --------------------------
    // Check branch result
    // --------------------------

    val test = io.src1.asUInt -^ io.src2.asUInt
    val eq = (test === 0)
    val ltu = ~test.msb
    val lt = (io.src1.msb & ~io.src2.msb) | (~(io.src1.msb ^ io.src2.msb)) & test(config.xlen-1)

    val beq  = io.opcode(2 downto 1) === 0
    val blt  = io.opcode(2 downto 1) === 2
    val bltu = io.opcode(2 downto 1) === 3
    val inv  = io.opcode(0) // invert the regular result to calculate bne, bge, bgeu

    val preRes = beq & eq | blt & lt | bltu & ltu
    val res = Mux(inv, ~preRes, preRes)

    io.branchCtrl.valid := io.branch & res | io.jump
    io.branchCtrl.payload := io.addr
}


object BEUVerilog extends App {
    val config = RiscCoreConfig(32, 0x00000000, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(BEU(config))
}

