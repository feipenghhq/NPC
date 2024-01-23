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
    logic signed [XLEN:0]   op1_ext;
    logic signed [XLEN:0]   op2_ext;
    logic signed [XLEN:0]   div_result;
    logic signed [XLEN:0]   rem_result;
    logic [XLEN-1:0]        final_div_result;
    logic [XLEN-1:0]        final_rem_result;

   // corner condidtion
    logic usign_div;     // unsigned division
    logic div_by_zero;   // divide by zero
    logic overflow;      // overflow
    // -------------------------------------------
    // Main logic
    // -------------------------------------------

    `define MOST_NEGATIVE_INTEGER  {2'b11, {(XLEN-2){1'b0}}}

    assign usign_div = opcode[0];
    assign div_by_zero = op2 == 0;
    assign overflow = ~usign_div & (op2 == {(XLEN){1'b1}}) & (op1 == `MOST_NEGATIVE_INTEGER);

    assign op1_ext = usign_div ? {1'b0, op1} : {op1[XLEN-1], op1};
    assign op2_ext = usign_div ? {1'b0, op2} : {op2[XLEN-1], op2};

    generate
    if (ARCH == 0) begin: single_cycle
        assign div_result = $signed(op1_ext) / $signed(op2_ext);
        assign final_div_result = overflow    ? `MOST_NEGATIVE_INTEGER :
                                  div_by_zero ? {(XLEN){1'b1}} : div_result[XLEN-1:0];

        assign rem_result = $signed(op1_ext) % $signed(op2_ext);
        assign final_rem_result = overflow    ? 0 :
                                  div_by_zero ? op1 : rem_result[XLEN-1:0];

        assign result = opcode[1] ? final_rem_result : final_div_result;
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
