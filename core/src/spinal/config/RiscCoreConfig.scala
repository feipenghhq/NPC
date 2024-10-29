/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/6/2024
 *
 * ------------------------------------------------------------------------------------------------
 * RiscCoreConfig: RISC V Core relate configuration
 * ------------------------------------------------------------------------------------------------
 */

package config

import spinal.core._
import _root_.bus.Axi4Lite._

case class RiscCoreConfig(
    // ISA related parameter
    xlen: Int = 32,                     // Cpu data width
    pcRstVector: BigInt = 0x80000000L,  // Need to add L here: https://github.com/SpinalHDL/SpinalHDL/issues/1420
    nreg: Int = 32,                     // Number of register. 16 or 32

    // Other parameter
    separateSram: Boolean = false,      // use two separate SRAM for instruction and data
    axi4LiteConfig: Axi4LiteConfig,     // AXI4 Lite bus configuration

    // test related parameter
    ifuRreadyDelay: Int = 0,            // Add delay to IFU rready
    lsuRreadyDelay: Int = 0,            // Add delay to LSU rready
    lsuBreadyDelay: Int = 0,            // Add delay to LSU bready
) {
    def regidWidth = log2Up(nreg)
    def nbyte = xlen / 8

    def xlenBits = Bits(xlen bit)
    def xlenUInt = UInt(xlen bit)
    def xlenSInt = SInt(xlen bit)
    def regidUInt = UInt(regidWidth bit)

    assert(xlen == 32)
    assert(nreg == 32)
}
