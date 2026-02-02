package goodspace.bllsoneshot.global.security

import java.security.Principal

val Principal.userId: Long
    get() = this.name.toLong()
