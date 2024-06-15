/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 06/15/2024
 *
 * ------------------------------------------------------------------------------------------------
 * DPI Interface for CoreN
 * ------------------------------------------------------------------------------------------------
 */

module CoreNDPI #(
    parameter XLEN       = 32
)(
    input  logic              clk,
    input  logic              rst_b,

    input  logic              ebreak,
    input  logic              ecall,

    input  logic [XLEN-1:0]   pc,
    output logic [XLEN-1:0]   inst, // instruction

    input  logic              data_valid,
    input  logic              data_wen,
    input  logic [XLEN-1:0]   data_wdata,
    input  logic [XLEN-1:0]   data_addr,
    input  logic [XLEN/8-1:0] data_wstrb,
    output logic [XLEN-1:0]   data_rdata
);

    // -------------------------------------------
    // _Verilator DPI
    // -------------------------------------------
    `ifdef VERILATOR

        import "DPI-C" function void dpi_set_ebreak();
        import "DPI-C" function void dpi_pmem_read(input int pc, input int addr, output int rdata, input bit ifetch);
        import "DPI-C" function void dpi_pmem_write(input int pc, input int addr, input int data, input byte strb);
        import "DPI-C" function void dpi_strace(input int pc, input int code);

        // set ebreak
        always @(posedge clk) begin
            if (ebreak) begin
                dpi_set_ebreak();
            end
        end

        // fetch instruction
        always @(*) begin
            inst = 0;
            if (rst_b) dpi_pmem_read(pc, pc, inst, 1'b1);
        end

        // data memory write, we should only invoke the DPI function at the end of the clock
        // this is because once we call the function, the value in the C/C++ variable will change
        // immediately, but the data should really be written to memory at the end of the clock
        always @(posedge clk) begin
            if (data_valid) begin
                if (data_wen) begin
                    dpi_pmem_write(pc, data_addr, data_wdata, {4'b0, data_wstrb});
                end
            end
        end

        // data memory read, read takes 0 cycle for single cycle core we should read combinationally
        // Another thing is we want to make only ONE call to dpi_pmem_read otherwise we will mess up
        // with the memory trace function (same memory read will be traced multiple time)
        // NOTE: We need to make sure that in verilator the new cycle start with clock being high!!!
        always @(clk) begin
            data_rdata = 0;
            if (!clk && data_valid && !data_wen) begin
                dpi_pmem_read(pc, data_addr, data_rdata, 1'b0);
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

