package jhkim105.authzdemo.common

class UnauthorizedException(message: String) : RuntimeException(message)
class ForbiddenException(message: String) : RuntimeException(message)
class NotFoundException(message: String) : RuntimeException(message)
