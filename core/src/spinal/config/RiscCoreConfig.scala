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
    xlen: Int,
    pcRstVector: BigInt = 0x80000000L, // Need to add L here: https://github.com/SpinalHDL/SpinalHDL/issues/1420
    nreg: Int = 32,
    hasRv32M: Boolean = false,
    hasZicsr: Boolean = false,
) {
    def regidWidth = log2Up(nreg)
    def nbyte = xlen / 8

    def xlenBits = Bits(xlen bit)
    def xlenUInt = UInt(xlen bit)
    def xlenSInt = SInt(xlen bit)
    def regidUInt = UInt(regidWidth bit)

    def axi4LiteConfig  = Axi4LiteConfig(addrWidth = xlen, dataWidth = xlen),
}
