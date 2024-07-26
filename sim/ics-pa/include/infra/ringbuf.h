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
    int entry;      // ring buffer entry size
    int head;       // point to the current position of the ringbuf
} ringbuf;


ringbuf *ringbuf_create(int entry, int size);
void ringbuf_delete(ringbuf *rb);
void ringbuf_write(ringbuf *rb, char *str);
void ringbuf_print(ringbuf *rb);

#endif
