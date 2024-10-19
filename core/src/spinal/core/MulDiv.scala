/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 6/20/2024
 *
 * ------------------------------------------------------------------------------------------------
 * MulDiv: Multiplier and Divider
 * open: Currently just use the operator *, /, and % for the calculation.
 * Will need to replace them with the actual hardware logic
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._

case class MulDiv(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val opcode = in port Bits(3 bits)
        val src1 = in port config.xlenBits
        val src2 = in port config.xlenBits
        val result = out port config.xlenBits
    }
    noIoPrefix()

    // Calculate signed and unsigned mul/div needs different multiplier and divider.
    // To save resource, we want share the multiplier and divider logic for both
    // signed and unsigned operation. To achieve this, we add one additional bit to
    // the original input and extend the original input. For signed operation, we do
    // signed extension. For unsigned operation, we add 0 so the value are treated as
    // positive. With the new value, we can use one signed multiplier and divider.

    // Note: Currently just use the *, /, and % operator
    val multiplier = new Area {
        val mulSrc1 = SInt(config.xlen + 1 bits)
        val mulSrc2 = SInt(config.xlen + 1 bits)

        mulSrc1(config.xlen-1 downto 0) := io.src1.asSInt
        mulSrc1(config.xlen) := io.src1.msb & (io.opcode(1 downto 0) =/= B"11")

        mulSrc2(config.xlen-1 downto 0) := io.src2.asSInt
        mulSrc2(config.xlen) := io.src2.msb & (io.opcode(1 downto 0) === B"01" | io.opcode(1 downto 0) === B"00")

        val mulFullResult = mulSrc1 * mulSrc2
        val mulResult = mulFullResult(0, config.xlen bits)
        val mulhResult = mulFullResult(config.xlen, config.xlen bits)
    }

    val divider = new Area {
        val divSrc1 = SInt(config.xlen + 1 bits)
        val divSrc2 = SInt(config.xlen + 1 bits)

        divSrc1(config.xlen-1 downto 0) := io.src1.asSInt
        divSrc1(config.xlen) := io.src1.msb & ~io.opcode(0)

        divSrc2(config.xlen-1 downto 0) := io.src2.asSInt
        divSrc2(config.xlen) := io.src2.msb & ~io.opcode(0)

        val divResult = divSrc1 / divSrc2
        val remResult = divSrc1 % divSrc2

        // processing corner condition
        val mostNegInt = B(config.xlen bit, config.xlen-1 -> true, default -> false)
        val minusOne = B(config.xlen bit, default -> true)

        val unsign = io.opcode(0)
        val zero = io.src2 === 0
        val overflow = ~unsign & (io.src2 === minusOne) & (io.src1 === mostNegInt)

        val divResFinal = Mux(overflow, mostNegInt,
                          Mux(zero,     minusOne,
                                        divResult(0, config.xlen bit).asBits))
        val remResFinal = Mux(overflow, B(0, config.xlen bits),
                          Mux(zero,     io.src1,
                                        remResult(0, config.xlen bit).asBits))
    }

    switch(io.opcode) {
        is(B"000")                 {io.result := multiplier.mulResult.asBits}
        is(B"001", B"010", B"011") {io.result := multiplier.mulhResult.asBits}
        is(B"100", B"101")         {io.result := divider.divResFinal}
        is(B"110", B"111")         {io.result := divider.remResFinal}
    }
}
