/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 01/20/2024
 *
 * ------------------------------------------------------------------------------------------------
 */

module DIV #(
    parameter XLEN = 32,
    parameter ARCH = 0 // 0 - single cycle, 1 - multicycle
) (
    input  logic               clk,
    input  logic               rst_b,
    input  logic               in_valid,
    output logic               in_ready,
    input  logic [1:0]         opcode,
    input  logic [XLEN-1:0]    op1,
    input  logic [XLEN-1:0]    op2,
    output logic               out_valid,
    output logic [XLEN-1:0]    result
);

    // -------------------------------------------
    // Signal definition
    // -------------------------------------------
    logic [XLEN:0]     op1_ext;
    logic [XLEN:0]     op2_ext;
    logic [XLEN:0]   div_result;
    logic [XLEN:0]   rem_result;

    // -------------------------------------------
    // Main logic
    // -------------------------------------------

    assign op1_ext = opcode[0] ? {1'b0, op1} : {op1[XLEN-1], op1};
    assign op2_ext = opcode[0] ? {1'b0, op2} : {op2[XLEN-1], op2};

    generate
    if (ARCH == 0) begin: single_cycle
        // FIXME: Need special handle on divide by zero
        assign div_result = $signed(op1_ext) / $signed(op2_ext);
        assign rem_result = $signed(op1_ext) % $signed(op2_ext);
        assign result = opcode[1] ? rem_result[XLEN-1:0] : div_result[XLEN-1:0];
        assign out_valid = in_valid;
        assign in_ready = 1'b1;
    end
    else begin: no_impl
        initial begin
            $display("Not implemented architecture");
            $exit();
        end
    end
    endgenerate

endmodule
