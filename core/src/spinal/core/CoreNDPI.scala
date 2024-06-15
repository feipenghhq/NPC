/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/15/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Black box for CoreNDPI
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._

case class CoreNDPI (config: RiscCoreConfig) extends BlackBox {

  val generic = new Generic {
    val XLEN = config.xlen
  }

  val io = new Bundle {
    val clk = in port Bool()
    val rst_b = in port Bool()
    val ebreak = in port Bool()
    val ecall = in port Bool()
    val pc = in port config.xlenUInt
    val inst = out port config.xlenBits
    val data_valid = in port Bool()
    val data_wen = in port Bool()
    val data_wdata = in port config.xlenBits
    val data_addr = in port config.xlenUInt
    val data_wstrb = in port Bits(config.xlen/8 bits)
    val data_rdata = out port config.xlenBits
  }

  noIoPrefix()
  mapClockDomain(clock = io.clk, reset = io.rst_b)
}

