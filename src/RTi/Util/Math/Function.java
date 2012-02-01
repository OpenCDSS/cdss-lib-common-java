package RTi.Util.Math;

/**
This interface defines the methods necessary to define a function, for use
in generic code that needs to evaluate the function.
*/
public abstract interface Function
{

public abstract double evaluate ( double [] parameters );

}