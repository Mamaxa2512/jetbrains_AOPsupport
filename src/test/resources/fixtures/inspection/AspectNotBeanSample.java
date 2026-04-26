import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class AspectNotBeanSample {

    @Before("execution(* com.example..*(..))")
    public void beforeAdvice() {}
}
