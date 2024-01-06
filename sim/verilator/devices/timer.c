/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 01/02/2024
 *
 * ------------------------------------------------------------------------------------------------
 */

#include "config.h"

#ifdef CONFIG_HAS_TIMER
#include <sys/time.h>
#include "device.h"

#define TIMER_SIZE 8
#define TIMER_BASE RTC_ADDR
#define TIMER_END  (TIMER_BASE + TIMER_SIZE - 1)

static const char name[] = "timer";
static time_t start = 0;

inline static time_t _get_usec() {
    struct timeval tv;
    int rc = gettimeofday(&tv, NULL);
    Check(rc == 0, "Failed to get time");
    return tv.tv_sec * 1000000 + tv.tv_usec;
}

#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
void timer_callback(word_t addr, word_t data, bool is_write, byte_t *mmio) {
#pragma GCC diagnostic pop
    Check(!is_write, "timer only support read mode");
    time_t usec, current;
    current = _get_usec();
    usec = current - start;
    uint64_t *timer_regs = (uint64_t *) (mmio + (TIMER_BASE - MMIO_BASE));
    *timer_regs = usec;
}

void init_timer() {
    add_device(name, (void *) TIMER_BASE, (void *) TIMER_END, timer_callback);
    log_info("Initialized TIMER device");
    start = _get_usec();
}

#endif
