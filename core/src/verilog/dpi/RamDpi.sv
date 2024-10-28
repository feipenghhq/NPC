/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 06/22/2024
 *
 * ------------------------------------------------------------------------------------------------
 * RamDpi: Ram using verilog dpi
 * ------------------------------------------------------------------------------------------------
 */

module RamDpi #(
    parameter XLEN      = 32
) (
    input  logic                clk,
    input  logic                rst_b,
    input  logic                ifetch, // for traceing
    input  logic [XLEN-1:0]     pc,     // for traceing
    input  logic                valid,
    input  logic                write,
    input  logic [XLEN-1:0]     addr,
    input  logic [XLEN/8-1:0]   strobe,
    input  logic [XLEN-1:0]     wdata,
    output logic [XLEN-1:0]     rdata
);

    logic [XLEN-1:0] _rdata;

    // Delay one clock cycle for read data to mimic synchronous ram
    always @(posedge clk) begin
        if (!rst_b) rdata <= 0;
        else rdata <= _rdata;
    end

    // -------------------------------------------
    // _Verilator DPI
    // -------------------------------------------
    `ifdef VERILATOR

        import "DPI-C" function void dpi_pmem_read(input int pc, input int addr, output int rdata, input bit ifetch);
        import "DPI-C" function void dpi_pmem_write(input int pc, input int addr, input int wdata, input byte strobe);

        // data memory write, we should only invoke the DPI function at the end of the clock
        // this is because once we call the function, the value in the C/C++ variable will change
        // immediately, but the data should really be written to memory at the end of the clock
        always @(posedge clk) begin
            if (valid && rst_b) begin
                if (write) begin
                    dpi_pmem_write(pc, addr, wdata, {4'b0, strobe});
                end
            end
        end

        // data memory read, read takes 0 cycle for single cycle core we should read combinationally
        // Another thing is we want to make only ONE call to dpi_pmem_read otherwise we will mess up
        // with the memory trace function (same memory read will be traced multiple time)
        // NOTE: We need to make sure that in verilator the new cycle start with clock being high!!!
        always @(clk) begin
            _rdata = 0;
            if (!clk && valid && !write && rst_b) begin
                dpi_pmem_read(pc, addr, _rdata, ifetch);
            end
        end

    `endif

endmodule