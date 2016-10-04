using UnityEngine;
using System.Collections;
using UnityEngine.SceneManagement;

public class MainMenu : MonoBehaviour {

    public void NewGame()
    {
        GameManager.Instance.ScanQRCode(true);
        StartCoroutine(WaitForQR());
    }

    public void Quit()
    {
        Application.Quit();
    }

    IEnumerator WaitForQR()
    {
        yield return new WaitForSeconds(0.5f);
        GameManager.Instance.QRLoaded = false;
        this.gameObject.SetActive(false);
    }
}
