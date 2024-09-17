package com.zhigalko.common.projection;

import java.io.Serializable;

public record CustomerProjection(Long id,
                                 String name,
                                 String address) implements Serializable {
}
