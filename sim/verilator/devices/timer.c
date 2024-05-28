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
#ifdef CONFIG_TIMER_CLOCK_GETTIME
#include <time.h>
#else
#include <sys/time.h>
#endif
#include "device.h"

#define TIMER_SIZE 8
#define TIMER_BASE RTC_ADDR
#define TIMER_END  (TIMER_BASE + TIMER_SIZE - 1)

static const char name[] = "timer";
static time_t start = 0;

inline static time_t _get_usec() {
#ifdef CONFIG_TIMER_CLOCK_GETTIME
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC_COARSE, &now);
    uint64_t us = now.tv_sec * 1000000 + now.tv_nsec / 1000;
    return us;
#else
    struct timeval tv;
    int rc = gettimeofday(&tv, NULL);
    Check(rc == 0, "Failed to get time");
    uint64_t us = tv.tv_sec * 1000000 + tv.tv_usec;
    return us;
#endif
}

void timer_callback(word_t addr, word_t data, bool is_write, byte_t *mmio) {
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
