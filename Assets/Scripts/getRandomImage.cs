using UnityEngine;
using UnityEngine.UI;
using System.Collections;

public class getRandomImage : MonoBehaviour {

    public Image Mouse;
    public Image Bunny;
    public Image Pig;
    public Image Fox;
    public Image Frog;

	// Use this for initialization
	void Start () {

    }
	
	// Update is called once per frame
	void Update () {
	
	}

    public void enableRandomPic()
    {
        int rand = UnityEngine.Random.Range(0, 3);
        if(rand == 0)
        {
      
        }
        else if(rand == 1)
        {
     
        }
        else
        {

        }
    }
}
