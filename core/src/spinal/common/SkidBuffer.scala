/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 10/27/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Skid buffer:
 * Temporarily hold the data when downstream logic stall.
 * Used to buffer the data for the valid-ready handshake to improve the throughput
 * ------------------------------------------------------------------------------------------------
 */

package common

import spinal.core._
import spinal.lib._

case class SkidBuffer[T<:Data](payloadType: HardType[T]) extends Component {
    val io = new Bundle {
        val in = slave Stream(payloadType)
        val out = master Stream(payloadType)
    }

    val bufValid = Reg(Bool) init False
    val bufPayload = Reg(payloadType)

    // store the data into the internal buffer and set the buffer valid indicator when
    // there are request from the input and the output is stalled
    when(io.in.valid && io.in.ready && !io.out.ready) {
        bufValid := True
        bufPayload := io.in.payload
    }
    // clear the valid when output is ready to take the data
    .elsewhen(bufValid && io.out.ready) {
        bufValid := False
    }

    // We can take the incoming transaction when the buffer is empty, the request
    // is either forwarded to the output port if it is ready or store in the buffer
    // if the output port is not ready
    // When the buffer is full, then it can't take another request
    io.in.ready := ~bufValid

    io.out.valid := bufValid | io.in.valid
    io.out.payload := Mux(bufValid, bufPayload, io.in.payload)
}

