package com.atmko.skiptoit.utils

import java.util.concurrent.Executor

class AppExecutors(
    val diskIO: Executor,
    val mainThread: Executor
)