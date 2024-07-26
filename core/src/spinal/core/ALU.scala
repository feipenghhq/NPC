/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/10/2024
 *
 * ------------------------------------------------------------------------------------------------
 * ALU
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._

case class ALU(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val opcode = in port Bits(5 bits)
        val src1 = in port config.xlenBits
        val src2 = in port config.xlenBits
        val result = out port config.xlenBits
        val addResult = out port config.xlenUInt // dedicated add result to speed up other logic that use tha adder result
    }
    noIoPrefix()

    // ------------------------------
    // Calculate result
    // ------------------------------

    val source1 = io.src1.asUInt
    val source2 = io.src2.asUInt

    val xorResult = source1 ^ source2
    val orResult = source1 | source2
    val andResult = source1 & source2
    val luiResult = source2

    val srlResult = source1 >> source2(4 downto 0)
    val sraResult = source1.asSInt >> source2(4 downto 0)
    val sllResult = source1 |<< source2(4 downto 0)

    // Note: We could use just one adder for add/sub operation but since
    // we are not resource limited design, we will just use 2 adders
    val addResult = source1 + source2
    val subResult = source1 -^ source2

    // slt result can be calculated with the following cases instead of using source1 < source2
    // 1. src1 < 0 and src1 > 0
    // 2. src1 and src2 are both positive or negative and adder result is negative (src1 < src2)
    val sltResult = config.xlenUInt
    sltResult := 0
    sltResult(0) := source1.msb & ~source2.msb | (~(source1.msb ^ source2.msb)) & subResult(config.xlen - 1)

    // if there is carry, then src1 is smaller then src2 for sltu
    val sltuResult = config.xlenUInt
    sltuResult := 0
    sltuResult(0) := subResult.msb

    // ------------------------------
    // Mux out the output
    // ------------------------------
    val res = config.xlenUInt
    switch(io.opcode) {
        is(B"10000") {res := luiResult}
        is(B"00000") {res := addResult}
        is(B"01000") {res := subResult.resized}
        is(B"00001") {res := sllResult}
        is(B"00010") {res := sltResult}
        is(B"00011") {res := sltuResult}
        is(B"00100") {res := xorResult}
        is(B"00101") {res := srlResult}
        is(B"01101") {res := sraResult.asUInt}
        is(B"00110") {res := orResult}
        is(B"00111") {res := andResult}
        default {res := addResult}
    }
    io.result := res.asBits
    io.addResult := addResult
}
