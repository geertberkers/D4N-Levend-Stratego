﻿using System;
using System.Collections;
using System.Threading;
using UnityEngine;
using UnityEngine.Events;
using UnityEngine.SceneManagement;
using UnityEngine.UI;
using ZXing;

namespace dZine4D.Misc.QR
{
    /// <summary>
    /// Detects qr codes using the webcam/phone camera
    /// </summary>
    public class QRReader : MonoBehaviour
    {
        // .. ATTRIBUTES

        public string LastResult { get; private set; }


        [SerializeField]
        [Tooltip("An optional renderer component to display the camera feed.")]
        private Renderer OutputRenderer = null;
        [SerializeField]
        [Tooltip("An optional RawImage component to display the camera feed.")]
        private RawImage OutputImage = null;
        //[SerializeField]
        //[Tooltip("An optional text component to display the last qr decoding result.")]
        //private Text OutputText = null;
        [SerializeField]
        [Tooltip("Should we start decoding on awake?")]
        private bool EnableOnAwake = true;

        private WebCamTexture camTexture;
        private Thread qrThread;

        private int W = 512;
        private int H = 512;

        private Color32[] cameraFeedGrab;
        private bool isQuit;
        private bool isReaderEnabled;

        public GameObject WrongQR;

        private string prevResult;

        private bool scanned = false;
        private bool newGame = false;
        private bool wrong = false;

        Result result;

        // .. EVENTS

        [Serializable]
        public class QrCodeDetectedEvent : UnityEvent<string> { }
        public QrCodeDetectedEvent OnQrCodeDetected;


        // .. INITIALIZATION

        void Awake()
        {
            LastResult = string.Empty;

            camTexture = new WebCamTexture(W, H);

            if (OutputRenderer != null)
                OutputRenderer.material.mainTexture = camTexture;
            if (OutputImage != null)
                OutputImage.texture = camTexture;

            if (EnableOnAwake)
                EnableReader();

            qrThread = new Thread(DecodeQR);
            qrThread.Start();
        }


        // .. OPERATIONS

        public void EnableReader()
        {
            StopCoroutine("EnableReaderRoutine");
            StartCoroutine("EnableReaderRoutine");
        }

        public void DisableReader()
        {
            if (!isReaderEnabled)
                return;
            isReaderEnabled = false;

            LastResult = string.Empty;
            prevResult = string.Empty;
            cameraFeedGrab = null;

            camTexture.Pause();
        }

        public void SetOutputImage(RawImage image)
        {
            OutputImage = image;
            OutputImage.texture = camTexture;
        }

        public void SetOutputRenderer(Renderer renderer)
        {
            OutputRenderer = renderer;
            OutputRenderer.material.mainTexture = camTexture;
        }

        void OnSceneLoaded()
        {
            GameManager.Instance.QRLoaded = true;
        }

        IEnumerator WaitWrong()
        {
            yield return new WaitForSeconds(1.5f);
            GameManager.Instance.ClearRole();
            SceneManager.UnloadScene("QRScanner");
        }

        void Update()
        {
            if (wrong)
            {
                WrongQR.SetActive(true);
                StartCoroutine(WaitWrong());
            }

            if (newGame)
            {
                newGame = false;
                GameManager.Instance.startGame = false;
                GameManager.Instance.AssignTeam(result.ToString());
                GameManager.Instance.AssignNewRole();
                SceneManager.UnloadScene("QRScanner");
            }

            if (scanned)
            {
                scanned = false;
                GameManager.Instance.AssignNewRole();
                SceneManager.UnloadScene("QRScanner");
            }

            if (!isReaderEnabled)
                return;

            if (cameraFeedGrab == null)
            {
                cameraFeedGrab = camTexture.GetPixels32();
            }

            if (!string.IsNullOrEmpty(LastResult) && LastResult != prevResult)
            {
                prevResult = LastResult;
                if (OnQrCodeDetected != null)
                    OnQrCodeDetected.Invoke(prevResult);

                //if (OutputText != null)
                //    OutputText.text = LastResult;
            }
        }

        void OnDestroy()
        {
            qrThread.Abort();
            camTexture.Stop();
        }

        // It's better to stop the thread by itself rather than abort it.
        void OnApplicationQuit()
        {
            isQuit = true;
        }

        void DecodeQR()
        {
            // create a reader with a custom luminance source
            var barcodeReader = new BarcodeReader { AutoRotate = false, TryHarder = false };

            while (true)
            {
                if (isQuit)
                    break;

                if (!isReaderEnabled)
                {
                    Thread.Sleep(200);
                    continue;
                }

                try
                {
                    // decode the current frame
                    result = barcodeReader.Decode(cameraFeedGrab, W, H);
                    if (result != null)
                    {
                        if (GameManager.Instance.startGame)
                            newGame = true;

                        if (result.ToString() != GameManager.Instance.Team)
                        {
                            wrong = true;
                            Debug.Log("result: " + result.ToString());
                            return;
                        } 

                        LastResult = result.Text;
                        print(result.Text);
                        scanned = true;
                    }

                    // Sleep a little bit and set the signal to get the next frame
                    Thread.Sleep(200);
                    cameraFeedGrab = null;
                }
                catch
                {
                }
            }
        }

        // .. COROUTINES

        IEnumerator EnableReaderRoutine()
        {
            if (isReaderEnabled)
                yield break;

            LastResult = string.Empty;
            prevResult = string.Empty;
            cameraFeedGrab = null;

            camTexture.Play();
            W = camTexture.width;
            H = camTexture.height;

            yield return new WaitForSeconds(0.5f);

            isReaderEnabled = true;
        }



    }
}
