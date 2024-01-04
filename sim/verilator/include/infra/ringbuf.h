// ------------------------------------------------------------------------------------------------
// Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
//
// Project: NRC
// Author: Heqing Huang
// Date Created: 01/03/2024
//
// ------------------------------------------------------------------------------------------------

#ifndef __INFRA_IRINGBUF_H_
#define __INFRA_IRINGBUF_H_

typedef struct ringbuf_node {
    char *buf;
    bool vld;
} ringbuf_node;

typedef struct ringbuf {
    ringbuf_node *node;
    int size;       // size of each ring buffer entry
    int len;        // length of the ring buffer (including null character)
    int head;       // point to the current position of the ringbuf
} ringbuf;


ringbuf *ringbuf_create(int len, int size);
void ringbuf_delete(ringbuf *rb);
void ringbuf_write(ringbuf *rb, char *str);
void ringbuf_print(ringbuf *rb);

#endif
