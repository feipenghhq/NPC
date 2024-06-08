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

case class RiscCoreConfig(
    xlen: Int,
    pcRstVector: Int,
    nreg: Int
) {
    def xlenUInt = UInt(xlen bit)
    def xlenBits = Bits(xlen bit)
    def regidWidth = log2Up(nreg)
}
