package org.pentaho.di.job.entry.loadSave;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author Andrey Khayrutdinov
 */
@Ignore
public abstract class JobEntryLoadSaveTestSupport<T extends JobEntryInterface> {

  private LoadSaveTester tester;

  @Before
  public void setUp() throws Exception {
    List<String> commonAttributes = listCommonAttributes();
    List<String> xmlAttributes = listXmlAttributes();
    List<String> repoAttributes = listRepositoryAttributes();
    Map<String, String> getters = createGettersMap();
    Map<String, String> setters = createSettersMap();
    Map<String, FieldLoadSaveValidator<?>> attributeValidators = createAttributeValidatorsMap();
    Map<String, FieldLoadSaveValidator<?>> typeValidators = createTypeValidatorsMap();

    assertTrue( !commonAttributes.isEmpty() || !( xmlAttributes.isEmpty() || repoAttributes.isEmpty() ) );

    tester = new LoadSaveTester( getJobEntryClass(), commonAttributes, xmlAttributes, repoAttributes, getters, setters,
      attributeValidators, typeValidators );
  }

  @Test
  public void xmlSerialization() throws Exception {
    tester.testXmlRoundTrip();
  }

  @Test
  public void repositorySerialization() throws Exception {
    tester.testRepoRoundTrip();
  }


  protected abstract Class<T> getJobEntryClass();

  protected abstract List<String> listCommonAttributes();


  protected List<String> listXmlAttributes() {
    return Collections.emptyList();
  }

  protected List<String> listRepositoryAttributes() {
    return Collections.emptyList();
  }

  protected Map<String, String> createGettersMap() {
    return Collections.emptyMap();
  }

  protected Map<String, String> createSettersMap() {
    return Collections.emptyMap();
  }

  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    return Collections.emptyMap();
  }

  protected Map<String, FieldLoadSaveValidator<?>> createTypeValidatorsMap() {
    return Collections.emptyMap();
  }


  @SuppressWarnings( "unchecked" )
  protected static <T1, T2> Map<T1, T2> toMap( Object... pairs ) {
    Map<T1, T2> result = new HashMap<T1, T2>( pairs.length );
    for ( int i = 0; i < pairs.length; i += 2 ) {
      T1 key = (T1) pairs[ i ];
      T2 value = (T2) pairs[ i + 1 ];
      result.put( key, value );
    }
    return result;
  }
}
