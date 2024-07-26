/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 07/03/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Testbench for ysyxSoC
 * ------------------------------------------------------------------------------------------------
 */

#include <verilated.h>
#include <verilated_vcd_c.h>
#include <VysyxSoCFull.h>
#include "autoconf.h"
#include "debug.h"
#include "soc.h"

#define VTOP VysyxSoCFull

void fill_flash();

//------------------------------------
// DPI
//------------------------------------

static bool dpi_ebreak = false;
static int32_t *inst;
static int32_t flash[FLASH_SIZE/4];

extern "C" void flash_read(int32_t addr, int32_t *data) {
    int index = addr >> 2;
    *data = flash[index];
}

extern "C" void mrom_read(int32_t addr, int32_t *data) {
    int index = (addr - MROM_OFFSET) >> 2;
    *data = inst[index];
}

extern "C" void dpi_set_ebreak() { dpi_ebreak = true; }

extern "C" void dpi_strace(int pc, int code) { }

//------------------------------------
// Testbench
//------------------------------------

class Testbench {

public:
    vluint64_t sim_time;
    VTOP *top;
    int reset_cycle;
    char *test;


    Testbench(int argc, char *argv[]);
    ~Testbench();

    void parse_args(int argc, char *argv[]);
    void load();
    void clk_tick();
    void reset();
    void run();
    bool check_finish();

    #ifdef CONFIG_WAVE
    VerilatedVcdC *m_trace;     // Waveform trace
    void init_dump(const char *name, int level);
    void dump();
    #endif
};

Testbench::Testbench(int argc, char *argv[]) {
    Verilated::commandArgs(argc, argv);
    parse_args(argc, argv);
    top = new VTOP();
    sim_time = 0;
    reset_cycle = 10;
    fill_flash();
    #ifdef CONFIG_WAVE
    VerilatedVcdC *m_trace;
    init_dump("waveform.vcd", 99);
    #endif
}

Testbench::~Testbench() {
    delete inst;
    delete top;
}

void Testbench::parse_args(int argc, char *argv[]) {
    if (argc < 1) {
        log_err("Wrong command.");
        log_info("Usage:\ntestbench program");
        exit(1);
    }
    test = argv[1];
}

void Testbench::load() {
    FILE *fp = fopen(test, "r");
    assert(fp);

    // read the file into local memory
    fseek(fp, 0, SEEK_END);
    size_t size = ftell(fp);
    rewind(fp);
    log_info("open file %s.", test);
    char *content = (char *) malloc(size);
    assert(content);
    int rc = fread(content, sizeof(char), size, fp);
    assert(rc == size);
    fclose(fp);

    inst = (int32_t *) content;
}

void Testbench::clk_tick() {
    top->clock ^= 1;
    top->eval();
    #ifdef CONFIG_WAVE
    dump();
    #endif
    sim_time++;
}

void Testbench::reset() {
    top->clock = 1; // initialize clock
    top->reset = 1;
    top->eval();
    sim_time++;
    for (int i = 0; i < reset_cycle; i++) {
        clk_tick();
    }
    top->reset = 0;
    assert(top->clock == 1); // we want to change data on negedge
}

bool Testbench::check_finish() {
    return dpi_ebreak;
}

void Testbench::run() {
    bool finish = false;
    load();
    reset();
    while (!finish) {
        clk_tick();
        clk_tick();
        finish = check_finish();
    }
    log_info("Test Finished");
}

#ifdef CONFIG_WAVE

void Testbench::init_dump(const char *name, int level) {
    Verilated::traceEverOn(true);
    m_trace = new VerilatedVcdC;
    top->trace(m_trace, level);
    m_trace->open(name);
}

void Testbench::dump() {
    if (m_trace && sim_time >= CONFIG_WAVE_START && sim_time <= CONFIG_WAVE_END) {
        m_trace->dump(sim_time);
    }
}

#endif

//------------------------------------
// Other functions
//------------------------------------
// fill flash for flash testing
#define FILL_SIZE 0x1000
void fill_flash() {
    for (int i = 0; i < FILL_SIZE; i++) {
        flash[i] = i;
    }
}

//------------------------------------
// Main function
//------------------------------------

int main(int argc, char *argv[]) {
    Testbench *tb = new Testbench(argc, argv);
    tb->run();
}