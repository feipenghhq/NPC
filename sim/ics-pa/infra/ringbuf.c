// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 01/03/2024
//
// ------------------------------------------------------------------------------------------------

#include <string.h>
#include "config.h"
#include "common.h"
#include "ringbuf.h"

// ----------------------------------------------
// Function
// ----------------------------------------------

ringbuf *ringbuf_create(int entry, int size) {
    ringbuf *rb = (ringbuf *) malloc(sizeof(ringbuf));
    CheckMalloc(rb);
    rb->size = size;
    rb->entry = entry;
    rb->head = entry - 1;
    rb->node = (ringbuf_node *) malloc(entry * sizeof(ringbuf_node));
    CheckMalloc(rb->node);
    for (int i = 0; i < entry; i++) {
        rb->node[i].buf = (char *) malloc(size * sizeof(char));
        CheckMalloc(rb->node[i].buf);
        rb->node[i].vld = false;
    }
    return rb;
}

void ringbuf_delete(ringbuf *rb) {
    for (int i = 0; i < rb->entry; i++) {
        free(rb->node[i].buf);
    }
    free(rb->node);
    free(rb);
}


inline static void ringbuf_inc_wrap(ringbuf *rb) {
    if (rb->head == (rb->entry - 1)) {
        rb->head = 0;
    }
    else {
        rb->head++;
    }
}

void ringbuf_write(ringbuf *rb, char *str) {
    ringbuf_inc_wrap(rb);
    char *buf = rb->node[rb->head].buf;
    char *rc = strcpy(buf, str);
    Check(rc, "Failed to write to ringbuf");
    rb->node[rb->head].vld = true;
}

void ringbuf_print(ringbuf *rb) {
    int curr = rb->head == (rb->entry - 1) ? 0 : rb->head + 1; // point curr to the next location of head
    for (; curr < rb->entry; curr++)
        if (rb->node[curr].vld) Log("     %s\n", rb->node[curr].buf);
    for (curr = 0; curr < rb->head; curr++)
        if (rb->node[curr].vld) Log("     %s\n", rb->node[curr].buf);

    Log("---> %s\n", rb->node[rb->head].buf);
    Log("\n");
}

