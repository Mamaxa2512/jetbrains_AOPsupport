import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class InvalidPointcutSample {

    @Pointcut("execution(* com.example..*(..))")
    public void validPointcut() {}

    @Pointcut("")
    public void emptyPointcut() {}

    @Before("execution(* *(..)) &&")
    public void trailingOperatorAdvice() {}

    @Before("unknown(* *(..))")
    public void unknownDesignatorAdvice() {}
}
