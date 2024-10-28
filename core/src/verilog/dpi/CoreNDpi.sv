/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 06/15/2024
 *
 * ------------------------------------------------------------------------------------------------
 * DPI Interface for CoreN
 * ------------------------------------------------------------------------------------------------
 */

module CoreNDpi #(
    parameter XLEN       = 32
)(
    input  logic              clk,
    input  logic              rst_b,

    input  logic              ebreak,
    input  logic              ecall,

    input  logic [XLEN-1:0]   pc
);


    // -------------------------------------------
    // _Verilator DPI
    // -------------------------------------------
    `ifdef VERILATOR

        import "DPI-C" function void dpi_set_ebreak();
        import "DPI-C" function void dpi_strace(input int pc, input int code);

        // set ebreak
        always @(posedge clk) begin
            if (ebreak) begin
                dpi_set_ebreak();
            end
        end

        // scall trace
        always @(posedge clk) begin
            if (ecall) begin
                dpi_strace(pc, 0); // syscall type is in a0 register, FIXME: can't pull regs[10] in spinal
            end
        end

    `endif

endmodule

