package RTi.Util.IO;

/**
This interface provides a listener for basic changes to Command lists.  It
can be used, for example, to allow domain-specific classes to notify UI
classes when commands have been added, removed, or changed, to allow appropriate
display changes to occur.
*/
public interface CommandListListener {

/**
Indicate when one or more commands have been added.
@param index0 The index (0+) of the first command that is added.
@param index1 The index (0+) of the last command that is added.
*/
public void commandAdded ( int index0, int index1 );

/**
Indicate when one or more commands have changed, for example in definition
or status.
@param index0 The index (0+) of the first command that is changed.
@param index1 The index (0+) of the last command that is changed.
*/
public void commandChanged ( int index0, int index1 );

/**
Indicate when one or more commands have been removed.
@param index0 The index (0+) of the first command that is removed.
@param index1 The index (0+) of the last command that is removed.
*/
public void commandRemoved ( int index0, int index1 );
	
}
